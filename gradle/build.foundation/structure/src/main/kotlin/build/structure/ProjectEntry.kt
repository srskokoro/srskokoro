package build.structure

import build.support.io.getFsSortingPrefixLength
import java.io.File
import java.util.LinkedList

internal const val D_BUILD_INCLUSIVE = ".build-inclusive"
internal const val D_BUILD_PLUGIN = ".build-plugin"

internal data class ProjectEntry(
	val parent: ProjectEntry?,
	val dirName: String,
) {
	@Transient private var relativePath_: String? = null
	@Transient private var projectId_: String? = null
	@Transient private var transitoryId_: String? = null

	val relativePath: String
		get() = relativePath_ ?: computeRelativePath()
			.also { relativePath_ = it }

	val projectId: String
		get() = projectId_ ?: computeProjectId()
			.also { projectId_ = it }

	val transitoryId: String
		get() = transitoryId_ ?: computeTransitoryId()
			.also { transitoryId_ = it }

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun into(output: LinkedList<ProjectEntry>): ProjectEntry {
		output.addLast(this)
		return this
	}

	private fun computeRelativePath(): String {
		val p = parent
		val name = dirName
		return if (p == null) name
		else p.relativePath + File.separatorChar + name
	}

	private fun computeProjectId(): String {
		val name = dirName
		val s = getProjectDirPrefixLength(name)

		val subStructureNameLength = getSubStructureNameLength(name)
		val e = name.length - subStructureNameLength

		return buildString {
			val p = parent
			if (p == null) {
				append(':')
			} else if (subStructureNameLength == 0) {
				append(p.projectId)
				append(':')
			} else {
				append(p.transitoryId)
			}
			append(name, s, e)
		}
	}

	private fun computeTransitoryId(): String {
		val name = dirName
		val s = getProjectDirPrefixLength(name)

		val subStructureNameLength = getSubStructureNameLength(name)
		val e = name.length - subStructureNameLength

		return buildString {
			val p = parent
			if (p == null) {
				append(':')
			} else {
				append(p.transitoryId)
			}
			append(name, s, e)
			append("[.]")
		}
	}
}

@Suppress("NOTHING_TO_INLINE")
private inline fun getProjectDirPrefixLength(dirName: String) = getFsSortingPrefixLength(dirName)

private fun getSubStructureNameLength(dirName: String): Int {
	return if (dirName.endsWith(D_BUILD_INCLUSIVE)) {
		D_BUILD_INCLUSIVE.length
	} else if (dirName.endsWith(D_BUILD_PLUGIN)) {
		D_BUILD_PLUGIN.length
	} else 0
}

// --

internal fun findIncludes(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	if (!name.endsWith(D_BUILD_INCLUSIVE) && !name.endsWith(D_BUILD_PLUGIN)) {
		findIncludes(file, ProjectEntry(parent, name).into(output), output)
	}
})

internal fun findBuildInclusives(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	ProjectEntry(parent, name).let { entry ->
		if (name.endsWith(D_BUILD_INCLUSIVE)) {
			findIncludesForBuildInclusive(file, entry.into(output), output)
		} else {
			// Recurse further. Consider only its subdirectories.
			findBuildInclusives(file, entry, output)
		}
	}
})

internal fun findIncludesForBuildInclusive(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	ProjectEntry(parent, name).let { entry ->
		if (name.endsWith(D_BUILD_PLUGIN)) {
			// Consider only its subdirectories
			findBuildInclusives(file, entry, output)
		} else {
			findIncludesForBuildInclusive(file, entry.into(output), output)
		}
	}
})

internal fun findBuildPlugins(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	ProjectEntry(parent, name).let { entry ->
		if (name.endsWith(D_BUILD_PLUGIN)) {
			findIncludesForBuildPlugin(file, entry.into(output), output)
		} else {
			// Recurse further. Consider only its subdirectories.
			findBuildPlugins(file, entry, output)
		}
	}
})

internal fun findIncludesForBuildPlugin(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	ProjectEntry(parent, name).let { entry ->
		if (name.endsWith(D_BUILD_INCLUSIVE)) {
			// Consider only its subdirectories
			findBuildPlugins(file, entry, output)
		} else {
			findIncludesForBuildPlugin(file, entry.into(output), output)
		}
	}
})

private fun findProjects(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
	onFound: (parent: ProjectEntry?, file: File, name: String, output: LinkedList<ProjectEntry>) -> Unit,
) {
	parentDir.list()?.forEach { name ->
		if (name.startsWith('.')) return@forEach
		when (name) {
			"build", "src", "test" -> return@forEach
		}

		val file = File(parentDir, name)
		if (!file.isDirectory) return@forEach
		if (!File(file, "build.gradle.kts").exists() && !File(file, "build.gradle").exists()) return@forEach

		// NOTE: Excludes those that look like included builds.
		if (File(file, "settings.gradle.kts").exists()) return@forEach
		if (File(file, "settings.gradle").exists()) return@forEach

		onFound(parent, file, name, output)
	}
}
