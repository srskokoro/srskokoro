@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("build-logic")
	repositories {
		mavenCentral()
		gradlePluginPortal()
		google()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		google()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}
}

rootProject.name = "srskokoro"

include(":common")
include(":android")
include(":desktop")
