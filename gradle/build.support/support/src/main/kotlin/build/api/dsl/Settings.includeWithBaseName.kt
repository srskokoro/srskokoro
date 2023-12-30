package build.api.dsl

import build.support.io.safeResolve
import org.gradle.api.initialization.Settings

fun Settings.includeWithBaseName(projectDir: String, baseName: String = rootProject.name) {
	val childId = buildString {
		if (!baseName.startsWith(':')) {
			append(':')
		}
		append(baseName)
		append('.')
		append(projectDir)
	}
	include(childId)
	project(childId).projectDir = settingsDir.safeResolve(projectDir)
}
