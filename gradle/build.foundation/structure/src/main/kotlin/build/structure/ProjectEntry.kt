package build.structure

import build.support.io.getFsSortingPrefixLength
import java.io.File
import java.util.LinkedList
import java.util.Properties

internal data class ProjectEntry(
	val parent: ProjectEntry?,
	val dirName: String,
	val transitory: Boolean,
) {
	@Transient private var cachedStructureRoot: File? = null
	@Transient private var projectDir_: File? = null
	@Transient private var projectId_: String? = null

	private fun cacheStructureRoot(structureRoot: File) {
		cachedStructureRoot?.let {
			if (it == structureRoot) return
			projectDir_ = null
			projectId_ = null
		}
		cachedStructureRoot = structureRoot
	}

	fun getProjectDir(structureRoot: File): File {
		cacheStructureRoot(structureRoot)
		projectDir_?.let { return it }

		return File(parent?.getProjectDir(structureRoot) ?: structureRoot, dirName)
			.also { projectDir_ = it }
	}

	fun getProjectId(structureRoot: File): String {
		cacheStructureRoot(structureRoot)
		projectId_?.let { return it }

		return buildString {
			if (!transitory) parent?.let { p ->
				append(p.getProjectId(structureRoot))
			}
			append(':')
			appendProjectName(structureRoot)
		}.also { projectId_ = it }
	}

	private fun StringBuilder.appendProjectName(structureRoot: File) {
		val propsFile = File(getProjectDir(structureRoot), "build.structure.properties")
		if (propsFile.exists()) propsFile.inputStream().use {
			Properties().apply { load(it) }.getProperty("project.name")
		}?.let {
			if (it.isNotBlank()) {
				append(it)
				return // Skip code below
			}
		}

		if (transitory) parent?.let { p ->
			val parentProjectId = p.getProjectId(structureRoot)
			for (i in 1 until parentProjectId.length) {
				val c = parentProjectId[i]
				if (c != ':') append(c)
				else append("[.]")
			}
			append("[.]")
		}

		dirName.let {
			val s = getFsSortingPrefixLength(it)
			append(it, s, it.length)
		}
	}

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun into(output: LinkedList<ProjectEntry>): ProjectEntry {
		output.addLast(this)
		return this
	}
}
