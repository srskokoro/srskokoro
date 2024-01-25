package build.kt.android.app

import build.api.ProjectPlugin
import build.api.dsl.accessors.androidApp
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("com.android.application")
		plugin<build.kt.android.internal._plugin>()
	}

	androidApp.run {
		packaging {
			resources {
				excludes += "/META-INF/{AL2.0,LGPL2.1}"
			}
		}
	}
})
