@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

gradle.allprojects {
	layout.buildDirectory.set(file(".build"))
}
