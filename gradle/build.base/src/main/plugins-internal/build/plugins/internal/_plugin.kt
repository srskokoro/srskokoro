package build.plugins.internal

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.internal._plugin>()
		plugin<build.support.kt.internal._plugin>()
		plugin<build.plugins.test.internal._plugin>()
	}
})
