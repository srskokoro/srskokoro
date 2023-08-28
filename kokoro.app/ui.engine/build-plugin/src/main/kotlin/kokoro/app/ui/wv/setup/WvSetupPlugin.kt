package kokoro.app.ui.wv.setup

import org.gradle.api.Plugin
import org.gradle.api.Project

class WvSetupPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.pluginManager.apply("conv.kt.mpp")
		project.pluginManager.apply("conv.kt.mpp.assets")

		// TODO
	}
}
