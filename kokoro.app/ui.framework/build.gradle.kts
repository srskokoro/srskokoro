plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("kokoro.app.ui.wv.setup")
	id("conv.redwood")
}

dependencies {
	commonMainImplementation(project(":kokoro:internal"))

	appMainImplementation(project(":kokoro.app:redwood:compose"))
	appMainImplementation(project(":kokoro.app:redwood:widget"))

	project(":kokoro.app:ui.engine").let {
		commonMainImplementation(it)
		commonMainWvSetup(it)
	}

	appMainImplementation("org.jetbrains.compose.runtime:runtime")
	appMainImplementation("org.jetbrains.compose.runtime:runtime-saveable")
	appMainImplementation("app.cash.redwood:redwood-widget")
}
