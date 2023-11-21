@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.ground")
	includeBuild("../build.logic")
}
plugins {
	id("conv.conventions.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build.logic")
}

gradle.rootProject {
	group = "convention"
}

rootProject.name = "conventions"
