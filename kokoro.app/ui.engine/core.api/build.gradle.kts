plugins {
	id("kokoro.conv.kt.mpp.lib")
}

dependencies {
	commonMainImplementation(project(":kokoro.lib.internal"))
}
