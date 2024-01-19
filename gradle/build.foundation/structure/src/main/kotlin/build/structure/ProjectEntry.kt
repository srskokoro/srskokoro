package build.structure

import build.support.io.getFsSortingPrefixLength
import java.io.File
import java.util.LinkedList

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

		val name = dirName
		val s = getFsSortingPrefixLength(name)

		return buildString {
			val p = parent
			if (p != null) {
				val parentProjectId = p.getProjectId(structureRoot)
				if (!transitory) {
					append(parentProjectId)
					append(':')
				} else {
					append(':')
					for (i in 1 until parentProjectId.length) {
						val c = parentProjectId[i]
						if (c != ':') append(c)
						else append("[.]")
					}
					append("[.]")
				}
			} else {
				append(':')
			}
			append(name, s, name.length)
		}.also { projectId_ = it }
	}

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun into(output: LinkedList<ProjectEntry>): ProjectEntry {
		output.addLast(this)
		return this
	}
}
