import convention.*

plugins {
	id("convention.kotlin.android.app")
	id("org.jetbrains.compose")
}

dependencies {
	setUpComposeDeps {
		implementation(it)
	}
	setUpComposePreviewDeps {
		// Needed only for preview
		implementation(it)
	}
}
