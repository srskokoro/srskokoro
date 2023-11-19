plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("kokoro.app.ui.wv.setup")
	id("conv.redwood")
}

dependencies {
	val unsafeParent = unsafeParent
	commonMainImplementation(project(":kokoro.lib:internal"))

	appMainImplementation(unsafeParent.project("redwood:compose"))
	appMainImplementation(unsafeParent.project("redwood:widget"))
	unsafeParent.project("ui.engine").let {
		commonMainImplementation(it)
		commonMainWvSetup(it)
	}

	appMainImplementation("org.jetbrains.compose.runtime:runtime")
	appMainImplementation("org.jetbrains.compose.runtime:runtime-saveable")
	appMainImplementation("app.cash.redwood:redwood-widget")
}
