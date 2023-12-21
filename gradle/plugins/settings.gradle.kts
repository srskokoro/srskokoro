@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
}
plugins {
	id("build.dependencies")
	id("build.dotbuild")
}

dependencyResolutionManagement {
	includeBuild("../build.logic")
	includeBuild("../support")
	repositories.gradlePluginPortal()
}
dependencySettings {
	includeBuild("../../dependencies")
}

include(":conventions")
project(":conventions").projectDir = file("../conventions")
