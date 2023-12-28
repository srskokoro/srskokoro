package build.kt.jvm

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin<build.kt.jvm.base._plugin>()
	}
})
