package build.kt.android.lib

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("com.android.library")
		plugin<build.kt.android.internal._plugin>()
	}
})
