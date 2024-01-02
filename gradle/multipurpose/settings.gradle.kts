@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../conventions")
	includeBuild("../build.support")
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
