@file:Suppress("UnstableApiUsage")

pluginManagement {
	extra["build.structure.disableOnIdeaInitialSync"] = true
	extra["build.structure.root"] = "../.."

	// NOTE: Must be first, so `Settings` plugins would be looked up here first,
	// and not cause any KMP projects to be built, which (at the moment) will
	// fail, since KMP requires the `rootProject` of the root of the composite
	// build (which isn't available yet while `settings` is being configured).
	includeBuild("../build.foundation")
	// NOTE: May have KMP projects.
	includeBuild("../conventions")

	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
	}
}
plugins {
	id("build.dependencies")
	id("build.foojay")
	id("build.structure.inclusives")
}

dependencySettings {
	includeBuild("../../dependencies")
}
dependencyResolutionManagement {
	repositories {
		mavenCentral()
		google()
	}
}

include("testing")
