import convention.internal.setup.*

plugins {
	id("convention.kotlin.mpp.lib")
	id("org.jetbrains.compose")
}

dependencies {
	setUpComposeDeps(compose) {
		commonMainImplementation(it)
	}
}
