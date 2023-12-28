package build.kt.jvm.base

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin(kotlin("jvm"))
		plugin<build.kt.jvm.internal._plugin>()
	}
})
