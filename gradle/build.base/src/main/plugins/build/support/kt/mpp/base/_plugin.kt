package build.support.kt.mpp.base

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp.lib.base._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}
})
