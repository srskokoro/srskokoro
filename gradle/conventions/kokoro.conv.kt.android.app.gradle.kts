import conv.internal.setup.*

plugins {
	id("conv.kt.android.app")
}

kotestConfigClass = "KotestConfig"

dependencies {
	val deps = deps ?: return@dependencies
	deps.bundles["testExtras"] *= {
		testImplementation(it)
	}
	testImplementation(project(":kokoro.lib.test.support"))
}
