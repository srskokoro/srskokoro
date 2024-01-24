@file:Suppress("UnstableApiUsage")

pluginManagement {
	extra["build.structure.root"] = "."

	includeBuild("../build.foundation")
	includeBuild("../build.foundation/core")

	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
	}
}
plugins {
	id("build.dependencies")
	id("build.foojay")
	id("build.structure")
}

dependencySettings {
	includeBuild("../../dependencies")
}
dependencyResolutionManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
	}
}
