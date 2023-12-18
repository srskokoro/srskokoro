package build.plugins

import build.api.ProjectPlugin
import build.api.dsl.model.api
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin {

	override fun Project.applyPlugin() {
		apply {
			plugin<build.plugins.base._plugin>()
		}

		dependencies {
			api("build:conventions.base")
		}
	}
}
