@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.base")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include(":support")
project(":support").projectDir = File(settingsDir, "../build.support")
include(":support:gradle")
