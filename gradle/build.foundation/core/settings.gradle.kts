@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

gradle.allprojects {
	layout.buildDirectory.set(file(".build"))
}

gradle.rootProject {
	group = "build.foundation"
}
