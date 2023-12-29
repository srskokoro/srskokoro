package build.kt.mpp

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("multiplatform"))
		plugin<build.kt.base.internal._plugin>()
	}
})
