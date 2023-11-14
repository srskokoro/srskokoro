plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("kokoro.app.ui.wv.setup")
	id("conv.redwood")
}

dependencies {
	commonMainApi(project("core.api"))
	desktopJvmMainApi(project("jcef"))

	commonMainImplementation(project(":kokoro.lib.internal"))

	appMainImplementation("com.soywiz.korlibs.kds:kds")

	appMainImplementation("org.jetbrains.compose.runtime:runtime")
	appMainImplementation("org.jetbrains.compose.runtime:runtime-saveable")
	appMainImplementation("app.cash.redwood:redwood-widget")

	appMainImplementation("cafe.adriel.voyager:voyager-core")
}
