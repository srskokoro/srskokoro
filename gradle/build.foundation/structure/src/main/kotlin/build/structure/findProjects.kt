package build.structure

import java.io.File
import java.util.LinkedList

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

// --

private const val BUILD_HOISTED_MARK = "build-hoisted.mark"
private const val BUILD_INCLUSIVE_MARK = "build-inclusive.mark"
private const val BUILD_PLUGIN_MARK = "build-plugin.mark"

internal fun findIncludes(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	run {
		if (File(file, BUILD_HOISTED_MARK).exists()) return@run
		if (File(file, BUILD_INCLUSIVE_MARK).exists()) return@run
		if (File(file, BUILD_PLUGIN_MARK).exists()) return@run

		findIncludes(file, ProjectEntry(parent, name, false).into(output), output)
	}
})

internal fun findBuildHoisted(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	val entry = ProjectEntry(parent, name, File(file, BUILD_HOISTED_MARK).exists())
	if (entry.transitory) {
		findBuildHoistedIncludes(file, entry.into(output), output)
	} else {
		// Recurse further. Consider only its subdirectories.
		findBuildHoisted(file, entry, output)
	}
})

internal fun findBuildHoistedIncludes(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	val entry = ProjectEntry(parent, name, File(file, BUILD_HOISTED_MARK).exists())
	run {
		if (!entry.transitory) {
			if (File(file, BUILD_INCLUSIVE_MARK).exists()) return@run
			if (File(file, BUILD_PLUGIN_MARK).exists()) return@run
		}
		findBuildHoistedIncludes(file, entry.into(output), output)
		return // Skip code below
	}
	// Consider only its subdirectories
	findBuildHoisted(file, entry, output)
})

internal fun findBuildInclusives(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	val entry = ProjectEntry(parent, name, File(file, BUILD_INCLUSIVE_MARK).exists())
	if (entry.transitory) {
		findBuildInclusiveIncludes(file, entry.into(output), output)
	} else {
		// Recurse further. Consider only its subdirectories.
		findBuildInclusives(file, entry, output)
	}
})

internal fun findBuildInclusiveIncludes(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	val entry = ProjectEntry(parent, name, File(file, BUILD_INCLUSIVE_MARK).exists())
	run {
		if (!entry.transitory) {
			if (File(file, BUILD_HOISTED_MARK).exists()) return@run
			if (File(file, BUILD_PLUGIN_MARK).exists()) return@run
		}
		findBuildInclusiveIncludes(file, entry.into(output), output)
		return // Skip code below
	}
	// Consider only its subdirectories
	findBuildInclusives(file, entry, output)
})

internal fun findBuildPlugins(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	val entry = ProjectEntry(parent, name, File(file, BUILD_PLUGIN_MARK).exists())
	if (entry.transitory) {
		findBuildPluginIncludes(file, entry.into(output), output)
	} else {
		// Recurse further. Consider only its subdirectories.
		findBuildPlugins(file, entry, output)
	}
})

internal fun findBuildPluginIncludes(
	parentDir: File, parent: ProjectEntry?, output: LinkedList<ProjectEntry>,
): Unit = findProjects(parentDir, parent, output, fun(parent, file, name, output) {
	val entry = ProjectEntry(parent, name, File(file, BUILD_PLUGIN_MARK).exists())
	run {
		if (!entry.transitory) {
			if (File(file, BUILD_HOISTED_MARK).exists()) return@run
			if (File(file, BUILD_INCLUSIVE_MARK).exists()) return@run
		}
		findBuildPluginIncludes(file, entry.into(output), output)
		return // Skip code below
	}
	// Consider only its subdirectories
	findBuildPlugins(file, entry, output)
})
