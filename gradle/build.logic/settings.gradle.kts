@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.base")
}
plugins {
	id("build.dotbuild")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}
