package build.kt.mpp.inclusive

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp.lib._plugin>()
		plugin<build.base.inclusive._plugin>()
	}
})
