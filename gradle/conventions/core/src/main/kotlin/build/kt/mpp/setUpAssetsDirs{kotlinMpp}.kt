package build.kt.mpp

import build.api.addExtraneousSourceTo
import build.api.dsl.*
import build.api.dsl.accessors.androidOrNull
import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.kpm.external.ExternalVariantApi
import org.jetbrains.kotlin.gradle.kpm.external.project
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
	val kotlinSourceSets = kotlin.kotlinSourceSets

	@OptIn(build.InternalApi::class)
	kotlinSourceSets.configureEach(fun KotlinSourceSet.() {
		val name = name
		this.project.objects.addExtraneousSourceTo(this, assets__extension, "$name assets (convention)")
			.srcDir("src/$name/$assets__extension")
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

	(@OptIn(ExternalVariantApi::class) kotlin.project).afterEvaluate(fun(p) {
		val android = p.androidOrNull
		val objects = p.objects

		kotlinSourceSets.configureEach(fun KotlinSourceSet.() {
			// Must wrap in a `Callable`, as AGP's implementation at the moment
			// eagerly evaluates `Iterable`s; also note that, `FileCollection`
			// is an `Iterable`.
			val assetsSrcDirs = assets.sourceDirectories.let { Callable { it } }

			@OptIn(ExperimentalKotlinGradlePluginApi::class)
			androidSourceSetInfoOrNull?.let { androidSourceSetInfo ->
				androidSourceSetInfo
					.getAndroidAssets(android!!)!!
					.srcDirs(assetsSrcDirs)
				return // Done. Skip code below.
			}

			resources.source(objects.sourceDirectorySet("--IDE-bridge--assets") {
				// NOTE: Task dependency information still needs to be
				// maintained, or Gradle may complain about task outputs being
				// reused, even though we're really not consuming the contents
				// of the assets directories.
				it.srcDir(assetsSrcDirs)
				it.exclude("*")
			})
		})

		kotlinTargets.withType<KotlinAndroidTarget>().configureEach {
			@Suppress("NAME_SHADOWING") val android = android!!
			compilations.configureEach { setUpAssetsAndResources(android) }
		}
	})
}

private inline fun <T : KotlinCompilation<*>> NamedDomainObjectContainer<T>.processAssetsAsResources(
	crossinline processResourcesTaskName: T.() -> String,
): Unit = configureEach {
	processAssetsAsResources(processResourcesTaskName())
}

private fun KotlinCompilation<*>.processAssetsAsResources(processResourcesTaskName: String) {
	val allKotlinSourceSets = allKotlinSourceSets
	this.project.tasks.named<ProcessResources>(processResourcesTaskName) {
		from(Callable { allKotlinSourceSets.mapNotNull { it.assetsOrNull } })
	}
}

private fun KotlinJvmAndroidCompilation.setUpAssetsAndResources(android: AndroidExtension) {
	val androidAssets = defaultSourceSet.getAndroidAssets(android)
		?: return // Skip (not for Android, or metadata/info not linked)

	val allKotlinSourceSets = allKotlinSourceSets
	val tasks = this.project.tasks

	// NOTE: We should ensure that the task's name is unique per compilation.
	// And thus, we can't use the compilation's default source set name (to be
	// the task's name), since (at the moment), it's (probably) possible for the
	// default source set to be reused across several compilations.
	val outputDirName = "${target.targetName}${compilationName.replaceFirstChar { it.uppercaseChar() }}"

	val prepareAssetsTaskName = "${outputDirName}PrepareCommonAssets"
	val prepareAssetsTask = tasks.register(prepareAssetsTaskName, ProcessResources::class.java) {
		description = "Processes assets from common (non-android) source sets"
		from(Callable {
			allKotlinSourceSets.mapNotNull {
				it.takeIf {
					@OptIn(ExperimentalKotlinGradlePluginApi::class)
					it.androidSourceSetInfoOrNull == null
				}?.assetsOrNull
			}
		})
		into(this.project.layout.buildDirectory.dir("preparedCommonAssets/$outputDirName"))
	}

	val variant = androidVariant
	buildMap {
		variant.mergeAssetsProvider.let { put(it.name, it) }
		tasks.namedOrNull(
			"merge${variant.name.replaceFirstChar { it.uppercaseChar() }}Assets"
		)?.let { put(it.name, it) }
	}.forEach(fun(_, taskProvider) = taskProvider.configure {
		dependsOn(prepareAssetsTask)
	})
	androidAssets.srcDirs(prepareAssetsTask) // Link output as Android-style "assets"

	// -=-

	// KLUDGE: At the moment, resources from common (non-android) kotlin source
	//  sets aren't automatically hooked to the android source set. Thus, we
	//  manually hook them here.
	variant.processJavaResourcesProvider.configure {
		from(Callable { allKotlinSourceSets.map { it.resources } })
	}
}
