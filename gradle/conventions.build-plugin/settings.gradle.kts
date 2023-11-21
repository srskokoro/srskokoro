@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../#root")
	includeBuild("../build-logic")
}
plugins {
	id("conv.conventions.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build-logic")
}

gradle.rootProject {
	group = "convention"
}

rootProject.name = "conventions"
