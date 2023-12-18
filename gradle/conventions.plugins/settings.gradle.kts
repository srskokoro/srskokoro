@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
}
plugins {
	id("build.dotbuild")
	id("build.settings.buildslist")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}
