pluginManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}
