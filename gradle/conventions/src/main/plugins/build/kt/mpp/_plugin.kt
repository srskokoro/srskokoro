package build.kt.mpp

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin<build.kt.mpp.base._plugin>()
	}
})
