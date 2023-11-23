@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.ground")
	includeBuild("../build.logic")
}
plugins {
	id("build.plugins.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build.logic")
}
