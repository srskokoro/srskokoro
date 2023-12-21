@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
}
plugins {
	id("build.dependencies")
	id("build.dotbuild")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}
dependencySettings {
	includeBuild("../../dependencies")
}
