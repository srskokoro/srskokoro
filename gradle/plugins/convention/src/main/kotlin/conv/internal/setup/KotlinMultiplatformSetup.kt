﻿package conv.internal.setup

import XS_assets
import assets
import getAndroidAssets
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.utils.ObservableSet
import java.util.concurrent.Callable

internal fun Project.setUp(kotlin: KotlinMultiplatformExtension) {
	setUpAssetsDir(this, kotlin) // Must be done first, so that the following subsequent setup may see it.
	setUp(kotlin as KotlinProjectExtension)
}

/**
 * Provides the ability to have common assets, as "resources" in JVM, but as
 * "assets" in Android.
 *
 * NOTE: While Java-style resources can be used in Android, it's generally
 * avoided for performance reasons. See the following:
 * - [Don't call Class.getResourceAsStream in firebase-iid · Issue #1601 · firebase/firebase-android-sdk | GitHub](https://github.com/firebase/firebase-android-sdk/issues/1601)
 * - [Tracking getResourceAsStream() performance · Issue #5369 · open-telemetry/opentelemetry-java | GitHub](https://github.com/open-telemetry/opentelemetry-java/issues/5369)
 * - [Strict mode violation · Issue #507 · signalfx/splunk-otel-android | GitHub](https://github.com/signalfx/splunk-otel-android/issues/507)
 * - [Why Is ClassLoader.getResourceAsStream So Slow in Android? - nimbledroid : r/androiddev | Reddit](https://www.reddit.com/r/androiddev/comments/4dmflo/why_is_classloadergetresourceasstream_so_slow_in/)
 */
private fun setUpAssetsDir(project: Project, kotlin: KotlinMultiplatformExtension) {
	val kotlinTargets = kotlin.targets
	kotlinTargets.withType<KotlinJvmTarget> {
		compilations.all {
			allKotlinSourceSets.initAssetsAsResources(project)
		}
	}
	project.ifAndroidProject {
		val android = project.androidExt
		kotlinTargets.withType<KotlinAndroidTarget> {
			compilations.all {
				allKotlinSourceSets.initAssetsAsResources(project)
				initConvAssetsProcessingTask()?.let { outputDir ->
					defaultSourceSet.getAndroidAssets(android)?.srcDir(outputDir)
				}
			}
		}
	}
}

private fun ObservableSet<KotlinSourceSet>.initAssetsAsResources(project: Project): Unit = forAll(fun KotlinSourceSet.() {
	@Suppress("OPT_IN_USAGE")
	if (androidSourceSetInfoOrNull != null) return // Skip (for Android)

	val extensions = (this as ExtensionAware).extensions
	if (extensions.findByName(XS_assets) != null) return // Skip. Already defined.

	val assetsDisplayName = "$name assets (conv)"
	val assets: SourceDirectorySet = project.objects.sourceDirectorySet(assetsDisplayName, assetsDisplayName)

	extensions.add<SourceDirectorySet>(XS_assets, assets)
	assets.srcDir(project.file("src/${this.name}/assets"))

	// Set up as an additional resources directory of the current source set
	resources.source(assets)
})

private fun KotlinJvmAndroidCompilation.initConvAssetsProcessingTask(): Provider<Directory>? {
	val outputDirName = "${target.targetName}${compilationName.replaceFirstChar { it.uppercaseChar() }}"
	val taskName = "${outputDirName}ProcessConvAssets"
	if (taskName in project.tasks.names) return null // Skip. Already defined.

	val outputDir: Provider<Directory> = project.layout.buildDirectory.dir("processedConvAssets/$outputDirName")
	project.tasks.register(taskName, @Suppress("UnstableApiUsage") ProcessResources::class.java) {
		description = "Processes assets (conv)"
		from(project.files(Callable { allKotlinSourceSets.mapNotNull { it.assets } }))
		into(outputDir)
	}
	return outputDir
}
