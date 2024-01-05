package build.structure.inclusives

import build.api.SettingsPlugin
import build.structure.autoIncludeBuildInclusiveSubProjects
import build.structure.getStructureRoot

class _plugin : SettingsPlugin({
	autoIncludeBuildInclusiveSubProjects(getStructureRoot(), "")
})
