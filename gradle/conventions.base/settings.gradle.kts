pluginManagement {
	includeBuild("../build.logic")
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	includeBuild("../build.logic")
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}
