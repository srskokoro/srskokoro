@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

gradle.rootProject {
	group = "build"
}

gradle.allprojects {
	layout.buildDirectory.set(file(".build"))
}
