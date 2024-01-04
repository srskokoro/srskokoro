@file:Suppress("UnstableApiUsage")

pluginManagement {
	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	autoGradleProperties("gradle/build.foundation/core")
	includeBuild(autoGradleProperties("gradle/build.foundation"))
	includeBuild(autoGradleProperties("gradle/conventions"))

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
	includeBuild(autoGradleProperties("gradle/multipurpose"))

	repositories {
		mavenCentral()
	}
}

rootProject.name = "srskokoro"
