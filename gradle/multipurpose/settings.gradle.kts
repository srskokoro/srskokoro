@file:Suppress("UnstableApiUsage")

pluginManagement {
	// NOTE: Must be first, so `Settings` plugins would be looked up here first,
	// and not cause any KMP projects to be built, which (at the moment) will
	// fail, since KMP requires the `rootProject` of the root of the composite
	// build (which isn't available yet while `settings` is being configured).
	includeBuild("../build.support")
	// NOTE: May have KMP projects.
	includeBuild("../conventions")

	repositories.gradlePluginPortal()
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	includeBuild("../../dependencies")
}
dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

include("testing")
