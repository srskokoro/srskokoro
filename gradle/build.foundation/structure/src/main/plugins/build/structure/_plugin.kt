package build.structure

import build.api.SettingsPlugin

class _plugin : SettingsPlugin({
	val structureRoot = getStructureRoot()
	include(ProjectStructure.INCLUDES.findProjects(structureRoot, providers), structureRoot)
})
