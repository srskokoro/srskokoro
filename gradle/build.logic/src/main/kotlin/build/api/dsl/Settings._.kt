package build.api.dsl

import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File

fun Settings.includeWithBaseName(projectDir: String, baseName: String = rootProject.name) {
	val childId = buildString {
		if (!baseName.startsWith(':')) {
			append(':')
		}
		append(baseName)
		append(projectDir.uppercaseFirstChar())
	}
	include(childId)
	project(childId).projectDir = File(settingsDir, projectDir)
}
