@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../settings")
}
plugins {
	id("convention.settings")
}

include(":convention")
include(":jcef-bundler")
