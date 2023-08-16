plugins {
	id("conv.sub")
	id("conv.kt.jvm.app")
	id("conv.version")
	id("jcef-bundler")
}

kotestConfigClass = "KotestConfig"

application {
	mainClass.set("main.MainKt")
}

afterEvaluate {
	tasks.withType<JavaExec>().configureEach {
		// Clear value inherited from `gradlew`; let it be null.
		environment.remove("APP_HOME")

		val jcefInstallTask = project.jcef.installTask.get()
		dependsOn(jcefInstallTask)
		systemProperty("jcef.bundle", jcefInstallTask.outputDir.get()
			.asFile.let { File(it, "jcef") })
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
	deps.bundles.testExtras *= {
		testImplementation(it)
	}
	testImplementation(project(":kokoro.lib.test.support"))

	implementation(project(":kokoro.app"))
	implementation(jcef.dependency)
}
