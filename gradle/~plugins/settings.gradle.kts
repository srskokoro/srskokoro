@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
}

dependencyResolutionManagement {
	includeBuild("../conventions")
	repositories.gradlePluginPortal()
}
