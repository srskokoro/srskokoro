@file:Suppress("UnstableApiUsage")

pluginManagement {
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

includeBuild("build-support")
include(":common", ":android", ":desktop")
