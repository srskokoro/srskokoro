package build.kt.jvm.lib

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm._plugin>()
		plugin("java-library")
	}
})
