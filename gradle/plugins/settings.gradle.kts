@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../meta-plugins")
}
plugins {
	id("convention.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../meta-plugins")
}
