import conv.sub.android.autoNamespace

plugins {
	id("kokoro.conv.kt.mpp.lib.sub")
	id("jcef-bundler-dependency")
}

kotestConfigClass = "KotestConfig"

val parent = project.parent!!
android.autoNamespace(parent)

dependencies {
	deps.bundles.testExtras *= {
		commonTestImplementation(it)
	}
	commonTestImplementation(project(":kokoro.lib.test.support"))

	commonMainImplementation(project(":kokoro.lib.internal"))

	commonMainImplementation(parent.project("redwood:compose"))
	commonMainImplementation(parent.project("redwood:widget"))

	desktopMainImplementation(jcef.dependency)

	commonMainImplementation("com.soywiz.korlibs.kds:kds")
}
