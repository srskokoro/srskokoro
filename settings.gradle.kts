@file:Suppress("UnstableApiUsage")

pluginManagement {
	extra["build.structure.root"] = rootDir

	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	autoGradleProperties("gradle/build.foundation/core")
	includeBuild(autoGradleProperties("gradle/build.foundation"))
	includeBuild(autoGradleProperties("gradle/conventions"))

	repositories.gradlePluginPortal()
}
plugins {
	id("build.dependencies")
	id("build.structure")
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
