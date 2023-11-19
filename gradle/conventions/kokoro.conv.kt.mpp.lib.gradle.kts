plugins {
	id("conv.kt.mpp.lib")
	id("kokoro.conv.kt.mpp.lib.hierarchy")
}

kotestConfigClass = "KotestConfig"

dependencies {
	commonTestImplementation(project(":kokoro:test.support"))
}
