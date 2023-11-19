plugins {
	id("conv.kt.jvm")
}

kotestConfigClass = "KotestConfig"

dependencies {
	testImplementation(project(":kokoro:test.support"))
}
