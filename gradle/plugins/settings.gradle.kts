@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.ground")
	includeBuild("../build.logic")
}
plugins {
	id("conv.plugins.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build.logic")
}
