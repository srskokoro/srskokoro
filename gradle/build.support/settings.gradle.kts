@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.foundation")
	repositories.gradlePluginPortal()
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include("core")
include("dependencies")
include("support")
include("testing")
