@file:Suppress("UnstableApiUsage")

pluginManagement {
	includeBuild("../meta-plugins/#plugins-root")
	includeBuild("../meta-plugins")
}
plugins {
	id("conv.plugins.settings")
}

dependencyResolutionManagement {
	repositories.gradlePluginPortal()
	includeBuild("../meta-plugins")
}

include("convention")
include("jcef-bundler")
include("redwood.ui.wv.setup")
