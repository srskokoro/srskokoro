@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("core")
	repositories.gradlePluginPortal()
}

dependencyResolutionManagement {
	includeBuild("core")
	repositories.gradlePluginPortal()
}

include("complement")
include("dependencies")
include("support")
include("testing")
