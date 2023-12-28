package build.kt.jvm.lib.base

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("java-library")
		plugin<build.kt.jvm.base._plugin>()
	}
})
