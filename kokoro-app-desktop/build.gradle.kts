plugins {
	id("convention.kotlin.jvm.app")
	id("jcef-bundler")
}

application {
	mainClass.set("MainKt")
}

dependencies {
	deps.bundles.testExtras {
		testImplementation(it)
	}
	implementation(project(":kokoro-app"))
	implementation(jcef.dependency)
}
