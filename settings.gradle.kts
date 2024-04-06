@file:Suppress("UnstableApiUsage")

import build.api.dsl.*

pluginManagement {
	apply(from = "gradle/discardInitialIdeaSync.settings.gradle.kts")
	extra["build.structure.root"] = "."

	apply(from = "gradle/autoGradleProperties.settings.gradle.kts")
	val autoGradleProperties: (String) -> String by extra

	autoGradleProperties("gradle/build.foundation/core")
	includeBuild(autoGradleProperties("gradle/build.foundation"))
	includeBuild(autoGradleProperties("gradle/conventions"))

	autoGradleProperties("gradle/hoisted")
	autoGradleProperties("gradle/inclusives")
	includeBuild(autoGradleProperties("gradle/plugins"))

	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
	}
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
	includeBuild("gradle/hoisted")
	includeBuild("gradle/inclusives")

	repositories {
		mavenCentral()
		google()
		maven("https://jitpack.io")

		// Download arbitrary files from GitHub releases.
		// - See, https://stackoverflow.com/a/34327202
		ivy("https://github.com/") {
			onlyIfAcceptedAs("githubReleases")
			patternLayout {
				// Dependency notation: "[organization]:[module]:[revision]:[classifier]@[ext]"
				artifact("/[organization]/[module]/releases/download/[revision]/[classifier](.[ext])")
			}
			metadataSources.artifact()
		}
	}

	// Self-include build to allow projects in the main build be addressable by
	// `${project.group}:${project.name}` coordinates.
	// - See also, https://docs.gradle.org/8.5/userguide/composite_builds.html#included_build_declaring_substitutions
	includeBuild(".")
}

rootProject.name = "srskokoro"
