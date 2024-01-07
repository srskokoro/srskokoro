package build.structure

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.File
import java.util.LinkedList

internal abstract class ProjectStructureSource : ValueSource<LinkedList<ProjectEntry>, ProjectStructureSource.Parameters> {

	interface Parameters : ValueSourceParameters {
		val structureRoot: Property<File>
		val finder: Property<ProjectStructure>
	}

	override fun obtain() = LinkedList<ProjectEntry>().also { out ->
		val structureRoot = parameters.structureRoot.get()
		val finder = parameters.finder.get()
		finder.findProjects(structureRoot, out)
	}
}
