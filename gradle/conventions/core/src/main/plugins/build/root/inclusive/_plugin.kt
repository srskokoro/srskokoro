package build.root.inclusive

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.root._plugin>()
	}

	allprojects(fun(project): Unit = project.afterEvaluate(fun(project) {
		// NOTE: Using `pluginManager.hasPlugin()` is preferred over
		// `plugins.hasPlugin()` according to `PluginAware.plugins` docs.
		val pluginManager = project.pluginManager
		if (pluginManager.hasPlugin(build.base.inclusive._plugin::class.java.packageName)) return
		if (pluginManager.hasPlugin(build.base._plugin::class.java.packageName)) error(
			"""
			Each project of this build should have "build.base.inclusive" plugin applied or
			must not have "build.base" plugin applied.
			""".trimIndent()
		)
	}))
})
