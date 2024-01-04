package build.root.inclusive

import build.api.ProjectPlugin
import build.base.inclusive._plugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.root._plugin>()
	}

	allprojects(fun(project): Unit = project.afterEvaluate(fun(project) {
		val plugins = project.plugins
		if (plugins.hasPlugin(_plugin::class.java)) return
		if (plugins.hasPlugin(build.base._plugin::class.java)) error(
			"""
			All projects of this build must have "build.base.inclusive" plugin applied or
			must not have "build.base" plugin applied.
			""".trimIndent()
		)
	}))
})
