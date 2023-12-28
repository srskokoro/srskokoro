package build.kt.mpp.lib

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
		plugin<build.kt.mpp.lib.base._plugin>()
	}
})
