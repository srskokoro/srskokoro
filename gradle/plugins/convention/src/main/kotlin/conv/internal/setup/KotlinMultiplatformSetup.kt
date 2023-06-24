﻿package conv.internal.setup

import XS_assets
import assets
import conv.internal.KotlinTargetsConfigLoader
import conv.internal.skipPlaceholderGenerationForKotlinTargetsConfigLoader
import getAndroidAssets
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.typeOf
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
import java.io.File
import java.util.UUID
import java.util.concurrent.Callable

internal fun Project.setUp(kotlin: KotlinMultiplatformExtension) {
	setUp(kotlin as KotlinProjectExtension)

	// The following makes sure that the type-safe accessors for extensions
	// added to `targets` are generated. See also, "Understanding when type-safe
	// model accessors are available | Gradle Kotlin DSL Primer | 7.5.1" --
	// https://docs.gradle.org/7.5.1/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
	//
	// NOTE: If one day, this would cause an exception due to "targets" already
	// existing, simply remove the following. It's likely that it's already
	// implemented for us, and if so, we shouldn't need to do anything.
	(kotlin as ExtensionAware).extensions.add(typeOf(), "targets", kotlin.targets)
}

internal fun Project.setUpTargetsExtensions(kotlin: KotlinMultiplatformExtension) {
	val config = layout.projectDirectory.file("build.targets.txt")
	KotlinTargetsConfigLoader(providers, config).loadInto(kotlin, skipPlaceholderGenerationForKotlinTargetsConfigLoader)
}

/**
 * Provides the ability to have common assets, as "resources" on JVM, but as
 * "assets" on Android.
 *
 * NOTE: While Java-style resources can be used in Android, it's generally
 * avoided for performance reasons. See the following:
 * - [Don't call Class.getResourceAsStream in firebase-iid · Issue #1601 · firebase/firebase-android-sdk | GitHub](https://github.com/firebase/firebase-android-sdk/issues/1601)
 * - [Tracking getResourceAsStream() performance · Issue #5369 · open-telemetry/opentelemetry-java | GitHub](https://github.com/open-telemetry/opentelemetry-java/issues/5369)
 * - [Strict mode violation · Issue #507 · signalfx/splunk-otel-android | GitHub](https://github.com/signalfx/splunk-otel-android/issues/507)
 * - [Why Is ClassLoader.getResourceAsStream So Slow in Android? - nimbledroid : r/androiddev | Reddit](https://www.reddit.com/r/androiddev/comments/4dmflo/why_is_classloadergetresourceasstream_so_slow_in/)
 */
internal fun Project.setUpAssetsDir(kotlin: KotlinMultiplatformExtension) {
	// The name of the dummy file in our dummy directory; Exclude on JVM but
	// throw if found on Android; Look out later below for further details.
	var dummyName: String? = null // Value will be set eventually (if needed)

	val kotlinTargets = kotlin.targets
	kotlinTargets.withType<KotlinJvmTarget> {
		compilations.all {
			val project = this.project
			initAssetsAsResources(allKotlinSourceSets, project)
			project.tasks.named<@Suppress("UnstableApiUsage") ProcessResources>(processResourcesTaskName) {
				exclude { it.name == dummyName && it.relativePath.segments.size <= 1 }
			}
		}
	}

	ifAndroidProject {
		// The following sets up a dummy directory as an additional "resources"
		// directory of `commonMain`. This dummy directory contains a dummy
		// file, which we exclude on JVM targets but throw if found on Android
		// targets. The purpose of this setup is to assert that our common
		// "assets" won't become Java-style "resources" on Android.
		dummyName = setUpConvAssetsDummy(kotlin)

		val android = androidExt
		kotlinTargets.withType<KotlinAndroidTarget> {
			compilations.all(fun KotlinJvmAndroidCompilation.() {
				setUpConvAssets(android)
				setUpConvAssetsDummyAssertion(dummyName)
			})
		}
	}
}

private fun initAssetsAsResources(
	allKotlinSourceSets: ObservableSet<KotlinSourceSet>,
	project: Project,
): Unit = allKotlinSourceSets.forAll(fun KotlinSourceSet.() {
	@Suppress("OPT_IN_USAGE")
	if (androidSourceSetInfoOrNull != null) return // Skip (for Android)

	val extensions = (this as ExtensionAware).extensions
	if (extensions.findByName(XS_assets) != null) return // Skip. Already defined.

	val assetsDisplayName = "$name assets (conv)"
	val assets: SourceDirectorySet = project.objects.sourceDirectorySet(assetsDisplayName, assetsDisplayName)

	extensions.add<SourceDirectorySet>(XS_assets, assets)
	assets.srcDir(project.file("src/$name/assets"))

	// Set up as an additional resources directory of the current source set
	resources.source(assets)
})

private fun KotlinJvmAndroidCompilation.setUpConvAssets(android: AndroidExtension) {
	val androidAssets = defaultSourceSet.getAndroidAssets(android)
		?: return // Skip (not for Android, or metadata/info not linked)

	val mergeAssetsTask = try {
		androidVariant.mergeAssetsProvider
	} catch (_: Exception) { // NOTE: Deliberately not `Throwable`
		return // Skip -- assume no "assets"
	}

	// NOTE: We should ensure that the task's name is unique per compilation.
	// And thus, we can't use the compilation's default source set name (to be
	// the task's name), since (at the moment), it's possible for the default
	// source set to be reused across several compilations.
	val outputDirName = "${target.targetName}${compilationName.replaceFirstChar { it.uppercaseChar() }}"
	val taskName = "${outputDirName}ProcessConvAssets"

	val project = project
	if (taskName in project.tasks.names) return // Skip (task already set up for this compilation, or task name conflict)

	val outputDir: Provider<Directory> = project.layout.buildDirectory.dir("processedConvAssets/$outputDirName")
	androidAssets.srcDir(outputDir) // Link output as Android-style "assets"

	val allKotlinSourceSets = allKotlinSourceSets
	initAssetsAsResources(allKotlinSourceSets, project)

	val task = project.tasks.register(taskName, @Suppress("UnstableApiUsage") ProcessResources::class.java) {
		description = "Processes assets (conv)"
		from(this.project.files(Callable { allKotlinSourceSets.mapNotNull { it.assets } }))
		into(outputDir)
	}
	mergeAssetsTask.configure { dependsOn(task) }
}

private fun KotlinJvmAndroidCompilation.setUpConvAssetsDummyAssertion(dummyName: String?) {
	try {
		androidVariant.processJavaResourcesProvider
	} catch (_: Exception) { // NOTE: Deliberately not `Throwable`
		return // Skip -- task not available
	}.configure {
		eachFile {
			if (name == dummyName && relativePath.segments.size <= 1) throw AssertionError(
				"""
				Common "assets" may have been included as Java-style "resources" on Android.
				This should not happen unless the Kotlin plugin now causes that to happen.
				Revision of build logic is thus necessary.
				""".trimIndent()
			)
		}
	}
}

private fun Project.setUpConvAssetsDummy(kotlin: KotlinMultiplatformExtension): String {
	val dummyDirProvider = layout.buildDirectory.dir("generated/convAssetsDummy")

	getSourceSets(kotlin).getByName("commonMain")
		.resources.srcDir(dummyDirProvider)

	@Suppress("UnstableApiUsage")
	return providers.of(ConvAssetsDummyNameValueSource::class.java) {
		parameters.dummyDirProperty.set(dummyDirProvider)
	}.get()
}

@Suppress("UnstableApiUsage")
internal abstract class ConvAssetsDummyNameValueSource :
	ValueSource<String, ConvAssetsDummyNameValueSource.Parameters> {

	interface Parameters : ValueSourceParameters {
		val dummyDirProperty: DirectoryProperty
	}

	override fun obtain(): String {
		val dummyDir = parameters.dummyDirProperty.get().asFile

		if (dummyDir.exists()) {
			dummyDir.list()?.let { names ->
				var dummyName: String? = null
				for (it in names) if (it.startsWith(DUMMY_NAME_PREFIX)) {
					if (dummyName == null) dummyName = it // First match found.
					else return@let // More than one entry matches. Skip below.
				}
				if (dummyName != null && dummyName.length == DUMMY_NAME_LENGTH) {
					return dummyName
				}
			}
			check(dummyDir.deleteRecursively()) { "Directory deletion failed: $dummyDir" }
		}
		dummyDir.mkdirs()

		val dummyName = UUID.randomUUID().let {
			DUMMY_NAME_PREFIX +
				it.mostSignificantBits.toBase36Padded() +
				it.leastSignificantBits.toBase36Padded()
		}
		if (dummyName.length != DUMMY_NAME_LENGTH)
			throw AssertionError("Unexpected")

		val dummyFile = File(dummyDir, dummyName)
		check(dummyFile.createNewFile()) { "File creation failed: $dummyFile" }

		return dummyName
	}
}

private fun Long.toBase36Padded(): String =
	toULong().toString(36).uppercase().padStart(BASE_36_PADDED_LENGTH, '0')

private const val BASE_36_PADDED_LENGTH = 13

private const val DUMMY_NAME_PREFIX = ".dummy#"
private const val DUMMY_NAME_PREFIX_LENGTH = DUMMY_NAME_PREFIX.length

private const val DUMMY_NAME_LENGTH = DUMMY_NAME_PREFIX_LENGTH + BASE_36_PADDED_LENGTH * 2
