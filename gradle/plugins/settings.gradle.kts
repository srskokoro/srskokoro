@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../meta-plugins")
}
plugins {
	id("conv.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../meta-plugins")
}
