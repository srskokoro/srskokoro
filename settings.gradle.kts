@file:Suppress("UnstableApiUsage")

pluginManagement {
	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	autoGradleProperties("gradle/build.base")
	includeBuild(autoGradleProperties("gradle/build.support"))

	repositories.gradlePluginPortal()
}

val autoGradleProperties: (String) -> String by extra
dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

rootProject.name = "srskokoro"
