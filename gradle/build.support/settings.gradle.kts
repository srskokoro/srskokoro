@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.base")
	repositories.gradlePluginPortal()
}

dependencyResolutionManagement {
	includeBuild(".") // -- https://docs.gradle.org/8.5/userguide/composite_builds.html#included_build_declaring_substitutions
	repositories.gradlePluginPortal()
}

include("support")
