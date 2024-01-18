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
	// coordinates (i.e., `${project.group}:${project.name}`), and also, so that
	// we can declare some dependency substitutions for the entire (main) build.
	// - See, https://docs.gradle.org/8.5/userguide/composite_builds.html#included_build_declaring_substitutions
	includeBuild(".") {
		dependencySubstitution {
			// Nothing (for now)
		}
	}
}

rootProject.name = "srskokoro"
