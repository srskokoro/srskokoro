@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build-logic/#root")
	includeBuild("../build-logic")
}
plugins {
	id("conv.conventions.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build-logic")
}

gradle.allprojects {
	group = "convention"
}

include(":conventions")
project(":conventions").projectDir = File(settingsDir, "../conventions.build-plugin")
