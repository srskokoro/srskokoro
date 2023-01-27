@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../settings")
}
plugins {
	id("convention.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}
