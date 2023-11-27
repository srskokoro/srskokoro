plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.ktx.atomicfu")
	id("conv.redwood")
}

dependencies {
	commonMainImplementation(project(":kokoro:internal"))

	commonMainImplementation(project(":kokoro.app:core.base"))

	appMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	desktopJvmMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

	appMainApi("org.jetbrains.compose.runtime:runtime")
	appMainApi("org.jetbrains.compose.runtime:runtime-saveable")
}
