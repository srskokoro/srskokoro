package build.kt.mpp.base

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin(kotlin("multiplatform"))
		plugin<build.kt.mpp.internal._plugin>()
	}
})
