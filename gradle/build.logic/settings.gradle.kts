@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build.ground")
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

include("build.support")
