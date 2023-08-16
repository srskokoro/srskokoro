@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("#plugins-root")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include("build-support", "deps", "settings")
