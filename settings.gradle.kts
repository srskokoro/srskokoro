@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("gradle/settings")
	includeBuild("gradle/plugins")
}
plugins {
	id("convention.settings")
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "srskokoro"

include(":common")
include(":android")
include(":desktop")
