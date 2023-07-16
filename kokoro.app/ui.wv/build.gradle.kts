import conv.sub.android.autoNamespace

plugins {
	id("kokoro.conv.kt.mpp.lib.sub")
}

val parent = project.parent!!
android.autoNamespace(parent)

dependencies {
	commonMainImplementation(project(":kokoro.lib.internal"))

	commonMainImplementation(parent.project("redwood:compose"))
	commonMainImplementation(parent.project("redwood:widget"))
}
