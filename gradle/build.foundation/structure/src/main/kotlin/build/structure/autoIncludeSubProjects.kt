package build.structure

import build.api.dsl.*
import build.support.from
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists

internal fun Settings.getStructureRoot(): Path {
	val structureRootValue = extra.getOrElse<Any>("build.structure.root") {
		error("Must set up structure root path via extra property key \"$it\" (or via 'gradle.properties' file)")
	}

	var structureRootPath = Path.of(structureRootValue.toString())
	if (!structureRootPath.isAbsolute) structureRootPath = rootDir.toPath() / structureRootPath
	structureRootPath = structureRootPath.normalize()

	// Guard against an unintended path set as the structure root
	if (!structureRootPath.div("settings.gradle.kts").exists() && !structureRootPath.div("settings.gradle").exists()) {
		error("Structure root must be a valid build with a Gradle `settings` file (even if empty)" +
			"\n- Structure root: $structureRootPath")
	}
	return structureRootPath
}

internal fun Settings.autoIncludeSubProjects(parentDir: Path, parentProjectId: String) {
	streamDir(parentDir) { p ->
		val name = dirNameOfAutoIncludedSubProject(p) ?: return@streamDir
		val s = getProjectDirPrefixLength(name)
		val e = name.length

		if (name.isSubStructureName(s, e, BUILD_INCLUSIVE)) return@streamDir
		if (name.isSubStructureName(s, e, BUILD_PLUGIN)) return@streamDir

		autoIncludeSubProjects(p, doIncludeSubProject(p, parentProjectId, name, s, e))
	}
}

internal fun Settings.autoIncludeBuildInclusiveSubProjects(parentDir: Path, projectIdPrefix: String) {
	streamDir(parentDir) { p ->
		val name = dirNameOfAutoIncludedSubProject(p) ?: return@streamDir
		val s = getProjectDirPrefixLength(name)
		var e = name.length

		if (name.isSubStructureName(s, e, BUILD_PLUGIN)) return@streamDir
		if (name.isSubStructureName(s, e, BUILD_INCLUSIVE)) {
			e -= BUILD_INCLUSIVE.length
			// Jump below. Do include.
		} else {
			// Recurse further. Consider only its subdirectories.
			autoIncludeBuildInclusiveSubProjects(p, "$projectIdPrefix${name.from(s)}$SUB_STRUCTURE_SEP")
			return@streamDir // Skip. Don't include.
		}

		// From here, include subprojects normally without special treatment.
		autoIncludeSubProjects(p, doIncludeSubProjectWithIdPrefix(p, projectIdPrefix, name, s, e))
	}
}

internal fun Settings.autoIncludeBuildPluginSubProjects(parentDir: Path, projectIdPrefix: String) {
	streamDir(parentDir) { p ->
		val name = dirNameOfAutoIncludedSubProject(p) ?: return@streamDir
		val s = getProjectDirPrefixLength(name)
		var e = name.length

		if (name.isSubStructureName(s, e, BUILD_INCLUSIVE)) return@streamDir
		if (name.isSubStructureName(s, e, BUILD_PLUGIN)) {
			e -= BUILD_PLUGIN.length
			// Jump below. Do include.
		} else {
			// Recurse further. Consider only its subdirectories.
			autoIncludeBuildPluginSubProjects(p, "$projectIdPrefix${name.from(s)}$SUB_STRUCTURE_SEP")
			return@streamDir // Skip. Don't include.
		}

		// From here, include subprojects normally without special treatment.
		autoIncludeSubProjects(p, doIncludeSubProjectWithIdPrefix(p, projectIdPrefix, name, s, e))
	}
}

// --

private inline fun streamDir(dir: Path, action: (Path) -> Unit) {
	// See also, https://github.com/gradle/gradle/issues/23638
	Files.newDirectoryStream(dir).use { dirStream -> dirStream.forEach(action) }
}

private fun dirNameOfAutoIncludedSubProject(path: Path): String? {
	val name = path.fileName?.toString()
	if (name.isNullOrEmpty() || name.startsWith('.') || name == "build" || !isPotentialSubProject(path)) {
		return null // Skip. Don't include.
	}
	return name
}

private fun getProjectDirPrefixLength(projectDirName: String) = when (projectDirName[0]) {
	// NOTE: Listed in sorted order.
	'!', '#', '$', '-', '@', '~' -> 1
	// The prefix can be a custom label in brackets
	'[' -> projectDirName.indexOf(']', 1) + 1 // -- returns `0` if not found
	else -> 0
}

private fun String.isSubStructureName(nameStart: Int, nameEnd: Int, substructureSuffix: String): Boolean {
	val x = nameEnd - substructureSuffix.length
	return startsWith(substructureSuffix, x, true) && (x <= nameStart || this[x - 1] == '.')
}

private fun Settings.doIncludeSubProject(currentPath: Path, parentProjectId: String, name: String, nameStart: Int, nameEnd: Int): String {
	val childProjectId = buildString {
		append(parentProjectId)
		append(':')
		append(name, nameStart, nameEnd)
	}
	include(childProjectId) // NOTE: Resolves relative to `Settings.rootDir`
	project(childProjectId).projectDir = currentPath.toFile()
	return childProjectId
}

private fun Settings.doIncludeSubProjectWithIdPrefix(currentPath: Path, projectIdPrefix: String, name: String, nameStart: Int, nameEnd: Int): String {
	val childProjectId = buildString {
		append(':')
		append(projectIdPrefix)
		append(name, nameStart, nameEnd)
	}
	include(childProjectId) // NOTE: Resolves relative to `Settings.rootDir`
	project(childProjectId).projectDir = currentPath.toFile()
	return childProjectId
}

// --

private const val BUILD_INCLUSIVE = "build-inclusive"
private const val BUILD_PLUGIN = "build-plugin"

private const val SUB_STRUCTURE_SEP = "[.]"

private val BUILD_GRADLE_KTS = Path.of("build.gradle.kts")
private val BUILD_GRADLE = Path.of("build.gradle")

private val SETTINGS_GRADLE_KTS = Path.of("settings.gradle.kts")
private val SETTINGS_GRADLE = Path.of("settings.gradle")

private fun isPotentialSubProject(dir: Path): Boolean {
	// See also, https://github.com/gradle/gradle/issues/23638
	return Files.isDirectory(dir)
		&& (dir.div(BUILD_GRADLE_KTS).exists() || dir.div(BUILD_GRADLE).exists())
		// NOTE: Excludes those that look like included builds.
		&& !dir.div(SETTINGS_GRADLE_KTS).exists()
		&& !dir.div(SETTINGS_GRADLE).exists()
}
