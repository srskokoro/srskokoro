@file:Suppress("UnstableApiUsage")

import build.api.dsl.*

pluginManagement {
	includeBuild("../build.base")
}
plugins {
	id("build.api.settings.base")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
}

includeWithBaseName("testing")
