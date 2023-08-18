plugins {
	id("conv.kt.jvm")
	id("kokoro.jcef.dependency")
}

kotestConfigClass = "KotestConfig"

dependencies {
	deps.bundles.testExtras *= {
		testImplementation(it)
	}
	testImplementation(project(":kokoro.lib.test.support"))

	implementation(project(":kokoro.lib.internal"))
	api(jcef.dependency)
}
