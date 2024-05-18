package build.kt.mpp

import build.api.addExtraneousSourceTo
import build.api.dsl.*
import build.api.dsl.accessors.android
import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.util.concurrent.Callable

/**
 * Provides the ability to have common assets as "resources" on non-Android
 * targets, and as Android-style "assets" on Android targets.
 *
 * NOTE: While Java-style resources can be used in Android, it's generally
 * avoided for performance reasons. See the following:
 * - [Don't call Class.getResourceAsStream in firebase-iid · Issue #1601 · firebase/firebase-android-sdk | GitHub](https://github.com/firebase/firebase-android-sdk/issues/1601)
 * - [Tracking getResourceAsStream() performance · Issue #5369 · open-telemetry/opentelemetry-java | GitHub](https://github.com/open-telemetry/opentelemetry-java/issues/5369)
 * - [Strict mode violation · Issue #507 · signalfx/splunk-otel-android | GitHub](https://github.com/signalfx/splunk-otel-android/issues/507)
 * - [Why Is ClassLoader.getResourceAsStream So Slow in Android? - nimbledroid : r/androiddev | Reddit](https://www.reddit.com/r/androiddev/comments/4dmflo/why_is_classloadergetresourceasstream_so_slow_in/)
 */
internal fun setUpAssetsDirs(kotlin: KotlinMultiplatformExtension) {
	@OptIn(build.InternalApi::class)
	kotlin.kotlinSourceSets.configureEach(fun KotlinSourceSet.() {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		if (androidSourceSetInfoOrNull != null) return // Skip (for Android)

		val objects = this.project.objects

		val name = name
		val assets = objects.addExtraneousSourceTo(this, assets__extension, "$name assets (convention)")
		assets.srcDir("src/$name/$assets__extension")

		resources.source(objects.sourceDirectorySet("--IDE-bridge--assets") {
			it.srcDir(Callable { assets.srcDirs })
			it.exclude("*")
		})
	})

	val kotlinTargets = kotlin.targets

	kotlinTargets.withType<KotlinJvmTarget>().configureEach {
		compilations.processAssetsAsResources { processResourcesTaskName }
	}
	kotlinTargets.withType<KotlinWithJavaTarget<*, *>>().configureEach {
		compilations.processAssetsAsResources { processResourcesTaskName }
	}
	kotlinTargets.withType<KotlinJsTargetDsl>().configureEach {
		compilations.processAssetsAsResources { processResourcesTaskName }
	}
	kotlinTargets.withType<KotlinNativeTarget>().configureEach {
		compilations.processAssetsAsResources { processResourcesTaskName }
	}

	kotlinTargets.withType<KotlinAndroidTarget>().configureEach {
		val android = this.project.android
		compilations.configureEach { setUpAssetsConvention(android) }
	}
}

private inline fun <T : KotlinCompilation<*>> NamedDomainObjectContainer<T>.processAssetsAsResources(
	crossinline processResourcesTaskName: T.() -> String,
): Unit = configureEach {
	processAssetsAsResources(processResourcesTaskName())
}

private fun KotlinCompilation<*>.processAssetsAsResources(processResourcesTaskName: String) {
	val allKotlinSourceSets = allKotlinSourceSets
	this.project.tasks.named<ProcessResources>(processResourcesTaskName) {
		from(this.project.files(Callable { allKotlinSourceSets.mapNotNull { it.assetsOrNull } }))
	}
}

private fun KotlinJvmAndroidCompilation.setUpAssetsConvention(android: AndroidExtension) {
	val androidAssets = defaultSourceSet.getAndroidAssets(android)
		?: return // Skip (not for Android, or metadata/info not linked)

	// NOTE: We should ensure that the task's name is unique per compilation.
	// And thus, we can't use the compilation's default source set name (to be
	// the task's name), since (at the moment), it's (probably) possible for the
	// default source set to be reused across several compilations.
	val outputDirName = "${target.targetName}${compilationName.replaceFirstChar { it.uppercaseChar() }}"
	val taskName = "${outputDirName}ProcessAssetsConvention"

	val allKotlinSourceSets = allKotlinSourceSets
	val task = this.project.tasks.register(taskName, ProcessResources::class.java) {
		description = "Processes assets (convention)"
		val project = this.project
		from(project.files(Callable { allKotlinSourceSets.mapNotNull { it.assetsOrNull } }))
		into(project.layout.buildDirectory.dir("processedAssetsConvention/$outputDirName"))
	}

	androidAssets.srcDirs(task) // Link output as Android-style "assets"
}
