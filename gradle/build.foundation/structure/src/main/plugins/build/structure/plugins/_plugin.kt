package build.structure.plugins

import build.api.SettingsPlugin
import build.structure.ProjectStructure
import build.structure.getStructureRoot
import build.structure.include

class _plugin : SettingsPlugin({
	include(ProjectStructure.BUILD_PLUGINS, getStructureRoot())
})
