import convention.*

plugins {
	id("convention.kotlin.jvm")
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
	implementation(compose.desktop.currentOs)
}
