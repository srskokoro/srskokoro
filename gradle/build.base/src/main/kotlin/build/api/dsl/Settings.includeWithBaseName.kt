package build.api.dsl

import org.gradle.api.initialization.Settings
import java.io.File

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
	project(childId).projectDir = File(settingsDir, projectDir)
}
