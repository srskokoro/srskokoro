@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.support")
	includeBuild("../build.foundation")
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
