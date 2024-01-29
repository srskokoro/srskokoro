package kokoro.build.kt.mpp.internal

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	kotlinMpp.androidTarget()
})
