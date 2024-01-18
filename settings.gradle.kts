@file:Suppress("UnstableApiUsage")

pluginManagement {
	extra["build.structure.root"] = "."

	apply(from = "gradle/autoGradleProperties.settings.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	autoGradleProperties("gradle/build.foundation/core")
	includeBuild(autoGradleProperties("gradle/build.foundation"))
	includeBuild(autoGradleProperties("gradle/conventions"))
	includeBuild(autoGradleProperties("gradle/plugins"))

	repositories.gradlePluginPortal()
}
plugins {
	id("build.dependencies")
	id("build.foojay")
	id("build.structure")
}

val autoGradleProperties: (String) -> String by extra
dependencySettings {
	includeBuild(autoGradleProperties("dependencies"))
}
dependencyResolutionManagement {
	includeBuild(autoGradleProperties("gradle/inclusives"))

	repositories {
		mavenCentral()
	}

	// Self-include build to allow projects in the main build be addressable by
	// `${project.group}:${project.name}` coordinates.
	// - See also, https://docs.gradle.org/8.5/userguide/composite_builds.html#included_build_declaring_substitutions
	includeBuild(".")
}

rootProject.name = "srskokoro"

project(":kokoro:internal.scoping").name = extra["kokoro.internal.scoping.artifact"] as String
project(":kokoro:internal.scoping:compiler").name = extra["kokoro.internal.scoping.compiler.artifact"] as String
