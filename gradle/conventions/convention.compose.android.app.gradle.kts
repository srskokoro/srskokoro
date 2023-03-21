import convention.internal.setup.*

plugins {
	id("convention.kotlin.android.app")
	id("org.jetbrains.compose")
}

dependencies {
	setUpComposeDeps(compose) {
		implementation(it)
	}
}
