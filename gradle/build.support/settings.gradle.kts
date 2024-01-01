@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.base")
	repositories.gradlePluginPortal()
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include("core")
include("dependencies")
include("support")
include("testing")
