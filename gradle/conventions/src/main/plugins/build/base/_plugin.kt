package build.base

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		if (project().parent == null) {
			plugin<build.root._plugin>()
		}
		plugin<build.base.base._plugin>()
	}
})
