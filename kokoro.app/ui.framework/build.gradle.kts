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

	appMainImplementation(parent.project("redwood:compose"))
	appMainImplementation(parent.project("redwood:widget"))
	parent.project("ui.engine").let {
		commonMainImplementation(it)
		commonMainWvSetup(it)
	}

	appMainImplementation("org.jetbrains.compose.runtime:runtime")
	appMainImplementation("org.jetbrains.compose.runtime:runtime-saveable")
	appMainImplementation("app.cash.redwood:redwood-widget")
}
