@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("gradle/settings")
	includeBuild("gradle/plugins")
}
plugins {
	id("convention.settings")
}

rootProject.name = "srskokoro"

include(":common")
include(":android")
include(":desktop")
