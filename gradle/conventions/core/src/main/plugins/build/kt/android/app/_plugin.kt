package build.kt.android.app

import build.api.ProjectPlugin
import build.api.dsl.accessors.androidApp
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	// Perhaps Android Studio prefers that we apply the Android plugin via
	// `pluginManager`, in order for source code to be analyzed as Android
	// sources (instead of for the current JDK running Gradle).
	// - See, https://github.com/android/nowinandroid/blob/0.1.2/build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt#L29
	pluginManager.apply("com.android.application")

	apply {
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
