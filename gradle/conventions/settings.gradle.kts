@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.support")
	includeBuild("../build.base")
	repositories.gradlePluginPortal()
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	includeBuild("../../dependencies")
}
dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

include("basic")
include("support")
include("support.kotlin-gradle-plugin")
include("testing")
