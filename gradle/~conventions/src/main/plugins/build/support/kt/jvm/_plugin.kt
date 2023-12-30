package build.support.kt.jvm

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.lib._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}
})
