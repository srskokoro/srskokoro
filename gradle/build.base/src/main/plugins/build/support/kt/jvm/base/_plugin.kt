package build.support.kt.jvm.base

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.lib.base._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}
})
