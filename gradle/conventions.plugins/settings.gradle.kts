@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
}
plugins {
	id("build.dotbuild")
}

dependencyResolutionManagement {
	includeBuild("../conventions.base")
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}
