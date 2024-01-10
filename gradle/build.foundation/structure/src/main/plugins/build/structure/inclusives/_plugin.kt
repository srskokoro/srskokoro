package build.structure.inclusives

import build.api.SettingsPlugin
import build.structure.ProjectStructure
import build.structure.findProjects
import build.structure.getStructureRoot
import build.structure.include

class _plugin : SettingsPlugin({
	val structureRoot = getStructureRoot()
	include(ProjectStructure.BUILD_INCLUSIVES.findProjects(structureRoot, providers), structureRoot)
})
