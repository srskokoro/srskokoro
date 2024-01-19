package build.structure.hoisted

import build.api.SettingsPlugin
import build.structure.ProjectStructure
import build.structure.include

class _plugin : SettingsPlugin({
	include(ProjectStructure.BUILD_HOISTED)
})
