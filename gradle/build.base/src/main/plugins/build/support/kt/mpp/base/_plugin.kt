package build.support.kt.mpp.base

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin(kotlin("multiplatform"))
		plugin<build.kt.mpp.lib.internal._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}
})
