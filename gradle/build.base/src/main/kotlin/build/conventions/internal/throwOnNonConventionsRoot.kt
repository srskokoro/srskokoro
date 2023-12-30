package build.conventions.internal

import org.gradle.api.Project

internal fun Project.throwOnNonConventionsRoot() {
	when (rootProject.name) {
		"build.support" -> {}
		"conventions" -> {}
		else -> error("This plugin has been forbidden for this build as it may affect the buildscript classpath")
	}
}
