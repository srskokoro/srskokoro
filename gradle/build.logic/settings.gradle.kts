@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.ground")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include("build.support", "deps", "settings")
