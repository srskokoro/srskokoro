package build.kt.jvm.inclusive

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.lib._plugin>()
		plugin<build.base.inclusive._plugin>()
	}
})
