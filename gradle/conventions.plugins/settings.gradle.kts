@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
}

dependencyResolutionManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}
