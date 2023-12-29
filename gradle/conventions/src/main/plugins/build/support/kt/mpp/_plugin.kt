package build.support.kt.mpp

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp.lib._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}
})
