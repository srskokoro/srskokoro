import convention.*

plugins {
	id("convention.kotlin.mpp.lib")
	id("org.jetbrains.compose")
}

dependencies {
	setUpComposeDeps(compose) {
		commonMainImplementation(it)
	}
}
