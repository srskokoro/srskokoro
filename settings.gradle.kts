@file:Suppress("UnstableApiUsage")

pluginManagement {
	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	autoGradleProperties("gradle/build.base")
	includeBuild(autoGradleProperties("gradle/build.logic"))
	includeBuild(autoGradleProperties("gradle/conventions"))
	includeBuild(autoGradleProperties("gradle/plugins"))

	repositories.gradlePluginPortal()
}
plugins {
	id("build.dependencies")
}

val autoGradleProperties: (String) -> String by extra
dependencySettings {
	includeBuild(autoGradleProperties("dependencies"))
}
dependencyResolutionManagement {
	includeBuild(autoGradleProperties("gradle/support"))

	repositories {
		mavenCentral()
	}
}

rootProject.name = "srskokoro"
