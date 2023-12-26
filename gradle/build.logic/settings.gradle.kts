@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.base")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include(":testing")
