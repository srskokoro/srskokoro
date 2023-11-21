@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.ground")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

gradle.allprojects {
	group = "build"
}

include("build.support", "dependencies", "settings")
