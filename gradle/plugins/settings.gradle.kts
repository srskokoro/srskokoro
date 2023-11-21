@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../#root")
	includeBuild("../build-logic")
}
plugins {
	id("conv.plugins.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build-logic")
}
