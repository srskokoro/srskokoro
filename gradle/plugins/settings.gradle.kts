@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build-logic/#root")
	includeBuild("../build-logic")
}
plugins {
	id("conv.plugins.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build-logic")
}

include(":conventions")
project(":conventions").projectDir = File(settingsDir, "../conventions.build-plugin")
