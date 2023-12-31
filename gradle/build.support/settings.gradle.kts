@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.base")
	repositories.gradlePluginPortal()
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include("dependencies")
include("support")
include("testing")
