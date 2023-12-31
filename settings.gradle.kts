@file:Suppress("UnstableApiUsage")

pluginManagement {
	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	autoGradleProperties("gradle/build.base")
	includeBuild(autoGradleProperties("gradle/build.support"))

	repositories.gradlePluginPortal()
}
plugins {
	id("build.dependencies")
}

val autoGradleProperties: (String) -> String by extra
dependencySettings {
	includeBuild("dependencies")
}
dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

rootProject.name = "srskokoro"
