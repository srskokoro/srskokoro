import convention.*

plugins {
	id("convention.kotlin.mpp.lib")
	id("org.jetbrains.compose")
}

dependencies {
	setUpComposeDeps {
		commonMainImplementation(it)
	}
	setUpComposePreviewDeps {
		// Needed only for preview
		commonMainImplementation(it)
	}
}
