package build.conventions.internal

import org.gradle.api.Project
import org.gradle.api.initialization.Settings

@InternalConventionsApi
fun Project.throwOnNonConventionsRoot() =
	throwOnNonConventionsRoot(rootProject.name)

@InternalConventionsApi
fun Settings.throwOnNonConventionsRoot() =
	throwOnNonConventionsRoot(rootProject.name)

private fun throwOnNonConventionsRoot(rootProjectName: String) {
	when (rootProjectName) {
		"build.support" -> {}
		"conventions" -> {}
		else -> error("This plugin has been forbidden for this build as it may affect the buildscript classpath")
	}
}
