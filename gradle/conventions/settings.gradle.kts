@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.foundation")
	includeBuild("../build.foundation/core")
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

include("core")
include("support")
include("support.kotlin-gradle-plugin")
include("testing")
include("testing.conventions")
