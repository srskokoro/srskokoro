package build.structure.plugins

import build.api.SettingsPlugin
import build.structure.autoIncludeBuildPluginSubProjects
import build.structure.getStructureRoot

class _plugin : SettingsPlugin({
	autoIncludeBuildPluginSubProjects(getStructureRoot(), "")
})
