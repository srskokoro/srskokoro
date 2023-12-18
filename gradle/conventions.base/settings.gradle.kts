@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
}

dependencyResolutionManagement {
	includeBuild("../build.logic")
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}
