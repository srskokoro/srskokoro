package kokoro.build.kt.mpp.internal

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.ensureMppHierarchyTemplateDefaultNodes
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	// Perhaps Android Studio prefers that we apply the Android plugin via
	// `pluginManager`, in order for source code to be analyzed as Android
	// sources (instead of for the current JDK running Gradle).
	// - See, https://github.com/android/nowinandroid/blob/0.1.2/build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt#L29
	pluginManager.apply("com.android.library")

	apply {
		plugin<build.kt.mpp._plugin>()

		plugin<build.kt.mpp.jre._plugin>()
		plugin<build.kt.mpp.ios._plugin>()
	}

	kotlinMpp.androidTarget()

	@OptIn(InternalApi::class)
	BuildFoundation.ensureMppHierarchyTemplateDefaultNodes(this)
})
