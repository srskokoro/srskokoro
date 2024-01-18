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
}

private class DependencySubstitutionsSetup(val project: Project) : Action<DependencySubstitutions> {

	private val kokoroLibGroup = project.extra["kokoro.group"] as String

	private fun DependencySubstitutions.setUp() {
		substitute(module("$kokoroLibGroup:${project.extra["kokoro.internal.scoping.compiler.artifact"]}"))
			.using(project(":kokoro:internal.scoping:compiler"))
		substitute(module("$kokoroLibGroup:${project.extra["kokoro.internal.scoping.artifact"]}"))
			.using(project(":kokoro:internal.scoping"))
	}

	override fun execute(dependencySubstitutions: DependencySubstitutions) = dependencySubstitutions.setUp()
}

gradle.allprojects {
	val dependencySubstitutionsSetup = DependencySubstitutionsSetup(this)
	configurations.configureEach {
		if (isCanBeResolved) resolutionStrategy {
			dependencySubstitution(dependencySubstitutionsSetup)
		}
	}
}

rootProject.name = "srskokoro"
