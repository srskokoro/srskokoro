@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
}
plugins {
	id("build.dependencies")
	id("build.dotbuild")
}

dependencyResolutionManagement {
	includeBuild("../conventions.base")
	repositories.gradlePluginPortal()
}
dependencySettings {
	includeBuild("../../dependencies")
}

include(":conventions.plugins")
project(":conventions.plugins").projectDir = file("../conventions.plugins")
