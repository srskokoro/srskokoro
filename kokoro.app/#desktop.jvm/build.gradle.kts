plugins {
	id("kokoro.conv.kt.jvm.app")
	id("conv.version")
	id("kokoro.jcef.bundler")
}

group = evaluatedParent.group

application {
	mainClass.set("main.MainKt")
	applicationDefaultJvmArgs = jcef.recommendedJvmArgs
	applicationName = extra["kokoro.app.exe.name"] as String
}

afterEvaluate {
	tasks.withType<JavaExec>().configureEach {
		val project = project
		val environment = environment

		// Clear value inherited from `gradlew`; let it be null.
		environment.remove("APP_HOME")

		val jcefInstallTask = project.jcef.installTask.get()
		dependsOn(jcefInstallTask)
		systemProperty("jcef.bundle", jcefInstallTask.outputDir.get()
			.asFile.let { File(it, "jcef") })

		val buildDir = project.buildDir
		environment["SRS_KOKORO_DATA"] = File(buildDir, "AppData/SRSKokoro-Dev")
		environment["SRS_KOKORO_COLLECTIONS_DEFAULT"] = File(buildDir, "AppData/SRS Kokoro (Dev)")
	}
}

distributions {
	main {
		contents {
			from(jcef.installTask) {
				include("jcef/**")
			}
		}
	}
}

dependencies {
	implementation(project(":kokoro.app"))
	implementation(jcef.dependency)
}
