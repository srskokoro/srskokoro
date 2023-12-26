@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}
