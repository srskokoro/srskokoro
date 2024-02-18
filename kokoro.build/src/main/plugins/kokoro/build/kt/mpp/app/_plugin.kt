package kokoro.build.kt.mpp.app

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinMpp
import build.foundation.BuildFoundation
import build.foundation.BuildFoundation.MPP
import build.foundation.InternalApi
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	// Perhaps Android Studio prefers that we apply the Android plugin via
	// `pluginManager`, in order for source code to be analyzed as Android
	// sources (instead of for the current JDK running Gradle).
	// - See, https://github.com/android/nowinandroid/blob/0.1.2/build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt#L29
	pluginManager.apply("com.android.library")

	apply {
		plugin<kokoro.build.kt.mpp.internal._plugin>() // Will set up Android target automatically
	}

	val kotlin = kotlinMpp

	@OptIn(InternalApi::class)
	kotlin.run {
		jvm(MPP.jre)

		if (BuildFoundation.shouldBuildNative(projectThis)) {
			iosX64()
			iosArm64()
			iosSimulatorArm64()
		} else projectThis.configurations.run {
			registerMppDummyConfigurations("ios")
			registerMppDummyConfigurations("apple")
			registerMppDummyConfigurations(MPP.unix)
			registerMppDummyConfigurations("native")
		}
	}
})

private fun ConfigurationContainer.registerMppDummyConfigurations(name: String) {
	register("${name}MainApi")
	register("${name}MainImplementation")
	register("${name}MainCompileOnly")
	register("${name}MainRuntimeOnly")
	register("${name}TestApi")
	register("${name}TestImplementation")
	register("${name}TestCompileOnly")
	register("${name}TestRuntimeOnly")
}
