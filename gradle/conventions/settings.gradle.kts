@file:Suppress("UnstableApiUsage")

pluginManagement {
	extra["build.structure.root"] = "."

	includeBuild("../build.foundation")
	includeBuild("../build.foundation/core")

	repositories.gradlePluginPortal()
}
plugins {
	id("build.dependencies")
	id("build.structure")
}

dependencySettings {
	includeBuild("../../dependencies")
}
dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}
