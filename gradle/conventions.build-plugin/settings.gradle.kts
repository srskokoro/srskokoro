@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.ground")
	includeBuild("../build.logic")
}
plugins {
	id("build.conventions.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build.logic")
}

gradle.rootProject {
	group = "build"
}
rootProject.name = "conventions"
