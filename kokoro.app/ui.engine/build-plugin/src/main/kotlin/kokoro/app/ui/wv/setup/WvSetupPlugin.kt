package kokoro.app.ui.wv.setup

import XS_wv
import addExtraneousSource
import conv.internal.setup.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class WvSetupPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.pluginManager.apply("conv.kt.mpp")
		project.pluginManager.apply("conv.kt.mpp.assets")

		val objects = project.objects
		val projectSourceSets = project.kotlinSourceSets

		projectSourceSets.all {
			val wvDisplayName = "$name WebView setup"
			val wv = objects.sourceDirectorySet(wvDisplayName, wvDisplayName)

			wv.include("**/*.wv.js")
			wv.include("**/*.wv.spec")
			wv.include("**/*.wv.lst")

			wv.srcDir("src/$name/wv")

			addExtraneousSource(XS_wv, wv)

			// Added simply for IDE support. Unnecessary otherwise.
			kotlin.source(wv)
		}

		// TODO Set up tasks
	}
}
