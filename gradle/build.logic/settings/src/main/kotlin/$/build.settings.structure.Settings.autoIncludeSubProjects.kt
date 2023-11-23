@file:Suppress("PackageDirectoryMismatch")

import build.internal.support.from
import org.gradle.api.initialization.Settings
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

private const val BUILD_PLUGIN_SUB_PROJECT = "build-plugin"
private const val BUILD_PLUGIN_SUB_PROJECT_len =
	/*         */ BUILD_PLUGIN_SUB_PROJECT.length


private val BUILD_GRADLE = Path.of("build.gradle")
private val BUILD_GRADLE_KTS = Path.of("build.gradle.kts")

private val SETTINGS_GRADLE = Path.of("settings.gradle")
private val SETTINGS_GRADLE_KTS = Path.of("settings.gradle.kts")

private fun isPotentialSubProject(dir: Path): Boolean {
	// NOTE: Excludes those that look like included builds.
	return Files.isDirectory(dir)
		&& !dir.resolve(SETTINGS_GRADLE_KTS).exists()
		&& !dir.resolve(SETTINGS_GRADLE).exists()
		&& (dir.resolve(BUILD_GRADLE_KTS).exists()
		|| dir.resolve(BUILD_GRADLE).exists())
}

/**
 * Incorporate convenient project directory name prefixes for affecting file
 * system sorting in the IDE's project view.
 */
private fun getProjectDirectoryPrefixLength(projectDirName: String) = when (projectDirName[0]) {
	'.', // Special

		// NOTE: Listed in sorted order.
	'#', '$', '+', '-', '@', '_', '~' -> 1
	else -> 0
}


internal fun Settings.autoIncludeSubProjects(parentDir: Path, parentProjectId: String) {
	Files.newDirectoryStream(parentDir).use(fun DirectoryStream<Path>.() = forEach(fun(path) {
		val name = path.fileName?.toString()
		if (name.isNullOrEmpty() || name.startsWith('.') || name == "build" || !isPotentialSubProject(path)) {
			return // Skip. Don't recurse further.
		}

		val nameStart = getProjectDirectoryPrefixLength(name)
		val nameLen = name.length
		run<Unit> {
			val checkIndex = nameLen - BUILD_PLUGIN_SUB_PROJECT_len
			if (name.startsWith(BUILD_PLUGIN_SUB_PROJECT, checkIndex) && (checkIndex <= nameStart || name[checkIndex - 1] == '.')) {
				return // Skip. Don't recurse further.
			}
		}

		val childProjectId = buildString {
			append(parentProjectId)
			append(':')
			append(name, nameStart, nameLen)
		}

		include(childProjectId) // Resolves relative to `settings.rootDir`
		project(childProjectId).projectDir = path.toFile()

		autoIncludeSubProjects(path, childProjectId)
	}))
}

internal fun Settings.autoIncludeBuildPluginSubProjects(parentDir: Path, projectIdPrefix: String) {
	Files.newDirectoryStream(parentDir).use(fun DirectoryStream<Path>.() = forEach(fun(path) {
		val name = path.fileName?.toString()
		if (name.isNullOrEmpty() || name.startsWith('.') || name == "build" || !isPotentialSubProject(path)) {
			return // Skip. Don't recurse further.
		}

		val nameStart = getProjectDirectoryPrefixLength(name)
		val nameEnd: Int
		val nameLen = name.length
		run<Unit> {
			val checkIndex = nameLen - BUILD_PLUGIN_SUB_PROJECT_len
			if (name.startsWith(BUILD_PLUGIN_SUB_PROJECT, checkIndex)) {
				if (checkIndex <= nameStart) {
					nameEnd = nameStart
					return@run // Proceed below
				} else {
					nameEnd = checkIndex - 1
					if (name[nameEnd] == '.') {
						return@run // Proceed below
					}
				}
			}
			// Recurse further. Consider only its subdirectories.
			autoIncludeBuildPluginSubProjects(path, "$projectIdPrefix${name.from(nameStart)}\$")
			return // Skip. Don't include.
		}

		val childProjectId = buildString {
			append(':')
			append(projectIdPrefix)
			append(name, nameStart, nameEnd)
		}

		include(childProjectId) // Resolves relative to `settings.rootDir`
		project(childProjectId).projectDir = path.toFile()

		// Include subprojects normally without special handling
		autoIncludeSubProjects(path, childProjectId)
	}))
}
