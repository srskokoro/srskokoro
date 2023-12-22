package build.support.kt.jvm

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

/**
 * WARNING: Before making changes to this plugin, please see first the NOTE
 * provided in [build.plugins.base._plugin], as that one is expected to apply
 * this [plugin][_plugin].
 */
class _plugin : ProjectPlugin {
	override fun Project.applyPlugin() {
		apply {
			plugin("java-library")
			plugin(kotlin("jvm"))
			plugin<build.support.kt.base._plugin>()
		}
	}
}
