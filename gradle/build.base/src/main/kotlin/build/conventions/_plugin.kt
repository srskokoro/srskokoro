package build.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.apply {
			plugin<build.conventions.support._plugin>()
			plugin<build.conventions.impl._plugin>()
		}
	}
}
