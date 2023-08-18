import conv.util.*

plugins {
	id("kokoro.conv.kt.mpp.lib")
}

kotestConfigClass = "KotestConfig"

val parent = project.parent!!
android.autoNamespace(project, parent)

dependencies {
	deps.bundles.testExtras *= {
		commonTestImplementation(it)
	}
	commonTestImplementation(project(":kokoro.lib.test.support"))

	commonMainImplementation(project(":kokoro.lib.internal"))

	commonMainImplementation(parent.project("redwood:compose"))
	commonMainImplementation(parent.project("redwood:widget"))

	desktopJvmMainApi(project("jcef"))

	commonMainImplementation("com.soywiz.korlibs.kds:kds")
}
