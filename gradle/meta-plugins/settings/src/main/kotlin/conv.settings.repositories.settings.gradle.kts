@file:Suppress("UnstableApiUsage")

pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
	}
}
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
//	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		google()
		maven("https://jitpack.io")
	}
}
