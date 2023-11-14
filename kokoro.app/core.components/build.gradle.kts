plugins {
	id("kokoro.conv.kt.mpp.lib")
}

dependencies {
	val parent = parent!!
	commonMainImplementation(parent.project("core.base"))

	commonMainImplementation(project(":kokoro.lib.internal"))
}
