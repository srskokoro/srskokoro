plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("kokoro.app.ui.wv.setup")
	id("conv.ktx.atomicfu")
	id("conv.redwood")
}

dependencies {
	commonMainApi(project("core.api"))

	commonMainImplementation(project(":kokoro:internal"))
	appMainImplementation("androidx.annotation:annotation")

	commonMainImplementation(project(":kokoro.app:core.base"))
	commonMainImplementation(project(":kokoro.app:core.components"))
	commonMainImplementation(project(":kokoro.app:core.compose"))

	appMainImplementation("com.soywiz.korlibs.kds:kds")

	appMainImplementation("org.jetbrains.compose.runtime:runtime")
	appMainImplementation("org.jetbrains.compose.runtime:runtime-saveable")
	appMainImplementation("app.cash.redwood:redwood-widget")

	appMainApi("org.jetbrains.kotlinx:kotlinx-serialization-core")

	appMainImplementation("cafe.adriel.voyager:voyager-core")
}
