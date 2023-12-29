package build.plugins.testing

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.api
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		if (isRoot) plugin<build.plugins.root._plugin>()
		plugin<build.plugins.testing.base._plugin>()
	}

	dependencies.run {
		api("build:build.logic")
	}
})
