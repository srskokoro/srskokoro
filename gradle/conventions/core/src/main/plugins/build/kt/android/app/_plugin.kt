package build.kt.android.app

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("com.android.application")
		plugin<build.kt.android.internal._plugin>()
	}
})
