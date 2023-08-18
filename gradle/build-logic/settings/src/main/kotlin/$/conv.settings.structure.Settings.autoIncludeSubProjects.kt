@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

private const val GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_1 = ".gradle-plugin"
private const val GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_1_len =
	/*         */ GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_1.length

private const val GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_2 = ".gradle-plugins"
private const val GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_2_len =
	/*         */ GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_2.length


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
	// NOTE: Listed in sorted order.
	'#', '$', '+', '-', '@', '_', '~' -> 1
	else -> 0
}


internal fun Settings.autoIncludeSubProjects(parentDir: Path, parentProjectId: String) {
	Files.newDirectoryStream(parentDir).use(fun DirectoryStream<Path>.() = forEach { path ->
		val name = path.fileName?.toString()
		if (name.isNullOrEmpty() || name.startsWith('.') || name == "build" || !isPotentialSubProject(path)) {
			return@forEach // Skip. Don't recurse further.
		}

		if (name.endsWith(GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_1) || name.endsWith(GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_2)) {
			return@forEach // Skip. Don't recurse further.
		}

		val childProjectId = buildString {
			append(parentProjectId)
			append(':')
			append(name, getProjectDirectoryPrefixLength(name), name.length)
		}

		include(childProjectId) // Resolves relative to `settings.rootDir`
		project(childProjectId).projectDir = path.toFile()

		autoIncludeSubProjects(path, childProjectId)
	})
}

internal fun Settings.autoIncludeGradlePluginSubProjects(parentDir: Path, projectIdPrefix: String) {
	Files.newDirectoryStream(parentDir).use(fun DirectoryStream<Path>.() = forEach { path ->
		val name = path.fileName?.toString()
		if (name.isNullOrEmpty() || name.startsWith('.') || name == "build" || !isPotentialSubProject(path)) {
			return@forEach // Skip. Don't recurse further.
		}

		val suffixLen = if (name.endsWith(GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_1)) {
			GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_1_len
		} else if (name.endsWith(GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_2)) {
			GRADLE_PLUGIN_SUB_PROJECT_SUFFIX_2_len
		} else {
			// Recurse further. Consider only its subdirectories.
			autoIncludeGradlePluginSubProjects(path, "$projectIdPrefix$name\$")
			return@forEach // Skip. Don't include.
		}

		val childProjectId = buildString {
			append(':')
			append(projectIdPrefix)
			append(name, getProjectDirectoryPrefixLength(name), name.length - suffixLen)
		}

		include(childProjectId) // Resolves relative to `settings.rootDir`
		project(childProjectId).projectDir = path.toFile()

		// Include subprojects normally without special handling
		autoIncludeSubProjects(path, childProjectId)
	})
}
