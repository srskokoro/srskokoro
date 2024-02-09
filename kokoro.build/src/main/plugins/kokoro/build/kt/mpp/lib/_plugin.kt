package kokoro.build.kt.mpp.lib

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import build.api.dsl.accessors.kotlinSourceSets
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.extendMppHierarchyTemplate
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.utils.ObservableSet

private const val LIB_PI_MAIN = "piMain" // "pi" stands for "plugin interface"
private const val LIB_PI_TEST = "piTest"

class _plugin : ProjectPlugin({
	// Perhaps Android Studio prefers that we apply the Android plugin via
	// `pluginManager`, in order for source code to be analyzed as Android
	// sources (instead of for the current JDK running Gradle).
	// - See, https://github.com/android/nowinandroid/blob/0.1.2/build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt#L29
	pluginManager.apply("com.android.library")

	@OptIn(InternalApi::class, ExperimentalKotlinGradlePluginApi::class)
	BuildFoundation.extendMppHierarchyTemplate(this) {
		common {
			group("lib") {
				withCompilations {
					when (it.name) {
						LIB_PI_MAIN, LIB_PI_TEST -> true
						else -> false
					}
				}
				group("host") {
					group(BuildFoundation.MPP.jvmish)
					group("native")
					group(BuildFoundation.MPP.desktop)
					group(BuildFoundation.MPP.mobile)
				}
			}
		}
	}

	apply {
		plugin<kokoro.build.kt.mpp.internal._plugin>() // Will set up Android target automatically
		plugin<build.kt.mpp.lib._plugin>()
	}

	val kotlin = kotlinMpp
	kotlin.run {
		js(IR) {
			// NOTE: Multiple 'same' targets are deprecated.
			// - The alternative is to simply create additional compilations.
			// - See, https://youtrack.jetbrains.com/issue/KT-59316
			setUpPiCompilations()
		}
	}
})

// NOTE: "pi" here stands for "plugin interface".
private fun KotlinJsTargetDsl.setUpPiCompilations() {
	val compilations = compilations
	val main = compilations.maybeCreate(LIB_PI_MAIN)
	val test = compilations.maybeCreate(LIB_PI_TEST)

	val kotlinSourceSets = project.kotlinSourceSets
	main.setUpAsAltTarget(kotlinSourceSets, kotlinSourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME))
	test.setUpAsAltTarget(kotlinSourceSets, kotlinSourceSets.getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME))

	test.associateWith(main)
	// TODO Actual test setup for the `test` compilation
	//  - See also, https://youtrack.jetbrains.com/issue/KT-59316
}

internal fun KotlinJsCompilation.setUpAsAltTarget(kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>, commonSourceSet: KotlinSourceSet) {
	binaries.executable(this)

	val defaultSourceSet = defaultSourceSet
	defaultSourceSet.dependsOn(commonSourceSet)

	val sourceSet = kotlinSourceSets.create(name)
	defaultSourceSet.dependsOn(sourceSet)
	(defaultSourceSet.dependsOn as ObservableSet).forAll {
		if (it != sourceSet) sourceSet.dependsOn(it)
	}
}
