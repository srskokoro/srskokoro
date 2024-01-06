package build.structure

import org.gradle.api.provider.ProviderFactory
import java.io.File
import java.util.LinkedList

// NOTE: Used an `enum` here, instead of `sealed` + `object`, for maximum
// compatibility with Gradle's configuration cache.
internal enum class ProjectStructure {
	INCLUDES {
		override fun findProjects(structureRoot: File, output: LinkedList<ProjectEntry>) {
			findIncludes(structureRoot, null, output)
		}
	},
	BUILD_INCLUSIVES {
		override fun findProjects(structureRoot: File, output: LinkedList<ProjectEntry>) {
			findBuildInclusives(structureRoot, null, output)
		}
	},
	BUILD_PLUGINS {
		override fun findProjects(structureRoot: File, output: LinkedList<ProjectEntry>) {
			findBuildPlugins(structureRoot, null, output)
		}
	};

	abstract fun findProjects(structureRoot: File, output: LinkedList<ProjectEntry>)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun ProjectStructure.findProjects(structureRoot: File, providers: ProviderFactory): LinkedList<ProjectEntry> {
	return providers.of(ProjectStructureSource::class.java) {
		val p = parameters
		p.structureRoot.set(structureRoot)
		p.finder.set(this@findProjects)
	}.get()
}
