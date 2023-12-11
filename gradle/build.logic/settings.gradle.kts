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

include(":support")
project(":support").projectDir = File(settingsDir, "../build.support")
include(":support:kt.dsl")
