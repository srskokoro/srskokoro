package build.dependencies

import build.api.SettingsPlugin
import build.api.dsl.*
import build.dependencies.DependencySettings.ExportMode.EXPORT_ONLY
import dependencySettings__name
import org.gradle.kotlin.dsl.*

class _plugin : SettingsPlugin({
	val dependencySettings = xs().create<DependencySettings>(dependencySettings__name, this)

	gradle.settingsEvaluated {
		val exportMode = dependencySettings.exportMode

		if (exportMode == null) {
			dependencySettings.cleanUpExport()
		} else {
			dependencySettings.setUpForExport()
		}

		if (exportMode != EXPORT_ONLY) {
			dependencySettings.setUpForUsageInProjects()
		}
	}
})
