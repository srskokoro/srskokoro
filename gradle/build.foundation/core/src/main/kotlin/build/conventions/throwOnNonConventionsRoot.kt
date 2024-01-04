package build.conventions

import build.foundation.InternalApi
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

@InternalApi
fun Project.throwOnNonConventionsRoot() =
	throwOnNonConventionsRoot(rootProject.name)

@InternalApi
fun Settings.throwOnNonConventionsRoot() =
	throwOnNonConventionsRoot(rootProject.name)

private fun throwOnNonConventionsRoot(rootProjectName: String) {
	when (rootProjectName) {
		"build.foundation" -> {}
		"conventions" -> {}
		else -> error("This plugin has been forbidden for this build as it may affect the buildscript classpath")
	}
}
