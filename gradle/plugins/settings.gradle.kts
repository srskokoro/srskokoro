@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../build-logic/#plugins-root")
	includeBuild("../build-logic")
}
plugins {
	id("conv.plugins.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../build-logic")
}

include("convention")
include("jcef-bundler")
include("redwood.ui.wv.setup")
