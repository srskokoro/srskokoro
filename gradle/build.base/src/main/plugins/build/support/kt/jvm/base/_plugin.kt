package build.support.kt.jvm.base

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("java-library")
		plugin(kotlin("jvm"))
		plugin<build.kt.jvm.internal._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}
})
