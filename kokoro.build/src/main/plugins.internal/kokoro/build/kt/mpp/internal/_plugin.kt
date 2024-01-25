package kokoro.build.kt.mpp.internal

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinMpp
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	onAndroid {
		kotlinMpp.androidTarget()
	}

	// NOTE: We're using `prioritizedAfterEvaluate()` so that we get to perform
	// our check (and perhaps throw) before KGP can do its own checks (which can
	// also throw) -- so that in case of errors, we get to throw first.
	prioritizedAfterEvaluate {
		check(pluginManager.hasPlugin("com.android.base")) {
			E_MUST_APPLY_ANDROID_PLUGIN_DIRECTLY
		}
	}
})

private const val E_MUST_APPLY_ANDROID_PLUGIN_DIRECTLY = """
Must directly apply an Android plugin (e.g., "com.android.library") via the
`plugins` block of the project's `build.gradle` file, so as to avoid a bug in
Android Studio which treats sources of Android targets as if they were for the
current JDK: the issue is that, the IDE's code analysis would fail for when a
class is referenced that's not present in Android targets but is present in the
current JDK (e.g., `java.lang.constant.Constable`).

Note that, the "current JDK" said above is the "Gradle JDK" used by Android
Studio for running Gradle and syncing projects. It's not the same as the JDK set
up via Gradle's JVM toolchain support. (At least, that's how things work at the
time of writing.)
"""
