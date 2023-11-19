plugins {
	id("conv.kt.android.app")
}

kotestConfigClass = "KotestConfig"

dependencies {
	testImplementation(project(":kokoro:test.support"))
}
