plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.ktx.atomicfu")
	id("conv.redwood")
}

dependencies {
	val unsafeParent = unsafeParent
	commonMainImplementation(unsafeParent.project("core.base"))

	commonMainImplementation(project(":kokoro.lib:internal"))

	appMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	desktopJvmMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

	appMainApi("org.jetbrains.compose.runtime:runtime")
	appMainApi("org.jetbrains.compose.runtime:runtime-saveable")
}
