@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("#root")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include("build-support", "deps", "settings")
