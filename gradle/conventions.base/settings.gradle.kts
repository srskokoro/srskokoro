@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
}
plugins {
	id("build.dotbuild")
}

dependencyResolutionManagement {
	includeBuild("../build.logic")
	repositories.gradlePluginPortal()
}
