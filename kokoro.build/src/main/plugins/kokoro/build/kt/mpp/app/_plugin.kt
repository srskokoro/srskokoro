package kokoro.build.kt.mpp.app

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinMpp
import build.api.dsl.accessors.kotlinSourceSets
import build.foundation.BuildFoundation
import build.foundation.BuildFoundation.MPP
import build.foundation.InternalApi
import build.foundation.extendMppHierarchyTemplate
import kokoro.build.kt.mpp.lib.setUpAsAltTarget
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

private const val APP_UI_MAIN = "uiMain"
private const val APP_UI_TEST = "uiTest"

class _plugin : ProjectPlugin({
	// Perhaps Android Studio prefers that we apply the Android plugin via
	// `pluginManager`, in order for source code to be analyzed as Android
	// sources (instead of for the current JDK running Gradle).
	// - See, https://github.com/android/nowinandroid/blob/0.1.2/build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt#L29
	pluginManager.apply("com.android.library")

	@OptIn(InternalApi::class, ExperimentalKotlinGradlePluginApi::class)
	BuildFoundation.extendMppHierarchyTemplate(this) {
		common {
			group("app") {
				withCompilations {
					when (it.name) {
						APP_UI_MAIN, APP_UI_TEST -> true
						else -> false
					}
				}
				group("host") {
					group(MPP.jvmish)
					group("native")
					group(MPP.desktop)
					group(MPP.mobile)
				}
			}
		}
	}

	apply {
		plugin<kokoro.build.kt.mpp.internal._plugin>() // Will set up Android target automatically
	}

	val kotlin = kotlinMpp
	kotlin.run {
		js(IR) {
			browser()

			// NOTE: Multiple 'same' targets are deprecated.
			// - The alternative is to simply create additional compilations.
			// - See, https://youtrack.jetbrains.com/issue/KT-59316
			setUpUiCompilations()
		}

		@OptIn(InternalApi::class)
		jvm(MPP.jre)

		@OptIn(InternalApi::class)
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

private fun KotlinJsTargetDsl.setUpUiCompilations() {
	val compilations = compilations
	val main = compilations.maybeCreate(APP_UI_MAIN)
	val test = compilations.maybeCreate(APP_UI_TEST)

	val kotlinSourceSets = project.kotlinSourceSets
	main.setUpAsAltTarget(kotlinSourceSets, kotlinSourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME))
	test.setUpAsAltTarget(kotlinSourceSets, kotlinSourceSets.getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME))

	test.associateWith(main)
	// TODO Actual test setup for the `test` compilation
	//  - See also, https://youtrack.jetbrains.com/issue/KT-59316
}
