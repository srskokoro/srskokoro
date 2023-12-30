@file:Suppress("UnstableApiUsage")

pluginManagement {
	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	includeBuild(autoGradleProperties("gradle/build.base")) // TODO Should not include this build directly

	repositories.gradlePluginPortal()
}

val autoGradleProperties: (String) -> String by extra
dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

rootProject.name = "srskokoro"
