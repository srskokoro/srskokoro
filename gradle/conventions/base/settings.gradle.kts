pluginManagement {
	includeBuild("../../build.logic")
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "conventions"
}
rootProject.name = "conventions.base"
