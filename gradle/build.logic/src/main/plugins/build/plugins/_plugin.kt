package build.plugins

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.api
import build.api.dsl.accessors.testImplementation
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		if (isRoot) plugin<build.plugins.root._plugin>()
		plugin<build.plugins.base._plugin>()
	}

	dependencies.run {
		api("build:build.logic")
		testImplementation("build:build.logic.testing")
	}
})
