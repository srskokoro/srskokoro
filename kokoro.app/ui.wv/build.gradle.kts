import conv.sub.android.autoNamespace

plugins {
	id("kokoro.conv.kt.mpp.lib.sub")
	id("jcef-bundler-dependency")
}

val parent = project.parent!!
android.autoNamespace(parent)

dependencies {
	commonMainImplementation(project(":kokoro.lib.internal"))

	commonMainImplementation(parent.project("redwood:compose"))
	commonMainImplementation(parent.project("redwood:widget"))

	desktopMainImplementation(jcef.dependency)

	commonMainImplementation("com.soywiz.korlibs.kds:kds")
}
