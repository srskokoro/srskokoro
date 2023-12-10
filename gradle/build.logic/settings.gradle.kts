@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.base")
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

gradle.allprojects {
	group = "build"
}
