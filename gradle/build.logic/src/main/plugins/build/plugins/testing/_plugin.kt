package build.plugins.testing

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.plugins.testing.base._plugin>()
	}
})
