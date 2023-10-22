import conv.util.*

plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("kokoro.app.ui.wv.setup")
	id("conv.redwood")
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

	desktopJvmMainApi(project("jcef"))

	commonMainImplementation("com.soywiz.korlibs.kds:kds")

	appMainImplementation("org.jetbrains.compose.runtime:runtime")
	appMainImplementation("org.jetbrains.compose.runtime:runtime-saveable")
	appMainImplementation("app.cash.redwood:redwood-widget")

	appMainImplementation("cafe.adriel.voyager:voyager-core")
}
