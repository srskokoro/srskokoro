package build.kt.mpp.android

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import org.gradle.kotlin.dsl.*

/**
 * @see build.kt.mpp.android.lib._plugin
 */
class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	kotlinMpp.androidTarget()
})
