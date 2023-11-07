import conv.internal.setup.*

plugins {
	id("conv.kt.mpp.lib")
	id("kokoro.conv.kt.mpp.lib.hierarchy")
}

kotestConfigClass = "KotestConfig"

dependencies {
	val deps = deps ?: return@dependencies
	deps.bundles["testExtras"] *= {
		commonTestImplementation(it)
	}
	commonTestImplementation(project(":kokoro.lib.test.support"))
}
