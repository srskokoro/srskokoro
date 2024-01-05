package build.structure

import build.api.SettingsPlugin

class _plugin : SettingsPlugin({
	autoIncludeSubProjects(getStructureRoot(), "")
})
