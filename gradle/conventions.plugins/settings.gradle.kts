pluginManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}
