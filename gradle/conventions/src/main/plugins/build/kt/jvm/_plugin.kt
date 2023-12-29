package build.kt.jvm

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("jvm"))
		plugin<build.kt.base.internal._plugin>()
	}
})
