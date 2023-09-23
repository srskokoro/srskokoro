package conv.internal.setup

import XS_assetsConv
import addExtraneousSource
import assets
import getAndroidAssets
import getExtraneousSourceOrNull
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.specs.Spec
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
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
import org.jetbrains.kotlin.gradle.utils.ObservableSet
import java.io.File
import java.util.UUID
import java.util.concurrent.Callable

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
	val objects = objects
	getSourceSets(kotlin).all(fun KotlinSourceSet.() { ensureAssets(objects) })

	// Contains the name of the dummy file in our dummy directory. Throw if
	// found on Android. Exclude if found on non-Android targets, e.g., JVM, JS,
	// etc. The value for the name is `null` for now, and will be set eventually
	// if actually needed. Look out later below for further details.
	val dummyHandler = DummyHandler()

	val kotlinTargets = kotlin.targets
	kotlinTargets.withType(fun KotlinJvmTarget.(): Unit = compilations.all(SetupToInitAssetsAsResourcesExcludingDummy(
		dummyHandler,
		getAllKotlinSourceSetsAsObservable = { allKotlinSourceSets },
		getProcessResourcesTaskName = { processResourcesTaskName },
	)))
	kotlinTargets.withType(fun KotlinWithJavaTarget<*, *>.(): Unit = compilations.all(SetupToInitAssetsAsResourcesExcludingDummy(
		dummyHandler,
		getAllKotlinSourceSetsAsObservable = { allKotlinSourceSets },
		getProcessResourcesTaskName = { processResourcesTaskName },
	)))
	kotlinTargets.withType(fun KotlinJsTargetDsl.(): Unit = compilations.all(SetupToInitAssetsAsResourcesExcludingDummy(
		dummyHandler,
		getAllKotlinSourceSetsAsObservable = { allKotlinSourceSets },
		getProcessResourcesTaskName = { processResourcesTaskName },
	)))
	kotlinTargets.withType(fun KotlinNativeTarget.(): Unit = compilations.all(SetupToInitAssetsAsResourcesExcludingDummy(
		dummyHandler,
		getAllKotlinSourceSetsAsObservable = { allKotlinSourceSets },
		getProcessResourcesTaskName = { processResourcesTaskName },
	)))

	ifAndroidProject {
		// The following sets up a dummy directory as an additional "resources"
		// directory of `commonMain`. This dummy directory contains a dummy
		// file, which we exclude on non-Android targets but throw if found on
		// Android targets. The purpose of this setup is to assert that our
		// common "assets" won't become Java-style "resources" on Android.
		dummyHandler.value = setUpAssetsConvDummy(kotlin)

		val android = androidExt
		kotlinTargets.withType<KotlinAndroidTarget> {
			compilations.all(fun KotlinJvmAndroidCompilation.() {
				setUpAssetsConv(android)
				setUpAssetsConvDummyAssertion(dummyHandler.value)
			})
		}
	}
}

private class DummyHandler : Action<@Suppress("UnstableApiUsage") ProcessResources>, Spec<FileTreeElement> {
	var value: String? = null

	override fun execute(task: @Suppress("UnstableApiUsage") ProcessResources) {
		task.exclude(this)
	}

	override fun isSatisfiedBy(elem: FileTreeElement) = elem.isDummyFile(value)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun FileTreeElement.isDummyFile(dummyName: String?) =
	name == dummyName && relativePath.segments.size <= 1

private abstract class SetupToInitAssetsAsResourcesExcludingDummy<T : KotlinCompilation<*>>(val dummyHandler: DummyHandler) : Action<T> {

	companion object {
		inline operator fun <T : KotlinCompilation<*>> invoke(
			dummyHandler: DummyHandler,
			crossinline getAllKotlinSourceSetsAsObservable: T.() -> ObservableSet<KotlinSourceSet>,
			crossinline getProcessResourcesTaskName: T.() -> String,
		) = object : SetupToInitAssetsAsResourcesExcludingDummy<T>(dummyHandler) {
			override fun getAllKotlinSourceSetsAsObservable(compilation: T) = getAllKotlinSourceSetsAsObservable(compilation)
			override fun getProcessResourcesTaskName(compilation: T) = getProcessResourcesTaskName(compilation)
		}
	}

	protected abstract fun getAllKotlinSourceSetsAsObservable(compilation: T): ObservableSet<KotlinSourceSet>

	protected abstract fun getProcessResourcesTaskName(compilation: T): String

	override fun execute(compilation: T) {
		val project = compilation.project
		initAssetsAsResources(getAllKotlinSourceSetsAsObservable(compilation), project)

		project.tasks.named(
			getProcessResourcesTaskName(compilation),
			@Suppress("UnstableApiUsage") ProcessResources::class,
			configurationAction = dummyHandler,
		)
	}
}

private fun KotlinSourceSet.ensureAssets(objects: ObjectFactory) = getExtraneousSourceOrNull(XS_assetsConv) ?: run {
	val assetsDisplayName = "$name assets (conv)"
	val assets: SourceDirectorySet = objects.sourceDirectorySet(assetsDisplayName, assetsDisplayName)

	addExtraneousSource(XS_assetsConv, assets)
	return assets
}

private fun initAssetsAsResources(
	allKotlinSourceSets: ObservableSet<KotlinSourceSet>,
	project: Project,
): Unit = allKotlinSourceSets.forAll(fun KotlinSourceSet.() {
	@OptIn(ExperimentalKotlinGradlePluginApi::class)
	if (androidSourceSetInfoOrNull != null) return // Skip (for Android)

	val assets = ensureAssets(project.objects)
	assets.srcDir(project.file("src/$name/assets"))

	// Set up as an additional resources directory of the current source set
	resources.source(assets)
})

private fun KotlinJvmAndroidCompilation.setUpAssetsConv(android: AndroidExtension) {
	val androidAssets = defaultSourceSet.getAndroidAssets(android)
		?: return // Skip (not for Android, or metadata/info not linked)

	// NOTE: We should ensure that the task's name is unique per compilation.
	// And thus, we can't use the compilation's default source set name (to be
	// the task's name), since (at the moment), it's (probably) possible for the
	// default source set to be reused across several compilations.
	val outputDirName = "${target.targetName}${compilationName.replaceFirstChar { it.uppercaseChar() }}"
	val taskName = "${outputDirName}ProcessAssetsConv"

	val project = project
	if (taskName in project.tasks.names) return // Skip (task already set up for this compilation, or task name conflict)

	val allKotlinSourceSets = allKotlinSourceSets
	initAssetsAsResources(allKotlinSourceSets, project)

	val task = project.tasks.register(taskName, @Suppress("UnstableApiUsage") ProcessResources::class.java) {
		description = "Processes assets (conv)"
		@Suppress("NAME_SHADOWING") val project = this.project
		from(project.files(Callable { allKotlinSourceSets.map { it.assets } }))
		into(project.layout.buildDirectory.dir("processedAssetsConv/$outputDirName"))
	}

	androidAssets.srcDirs(task) // Link output as Android-style "assets"
}

private fun KotlinJvmAndroidCompilation.setUpAssetsConvDummyAssertion(dummyName: String?) {
	try {
		androidVariant.processJavaResourcesProvider
	} catch (_: Exception) { // NOTE: Deliberately not `Throwable`
		return // Skip -- task not available
	}.configure {
		eachFile {
			if (isDummyFile(dummyName)) throw AssertionError(
				"""
				Common "assets" may have been included as Java-style "resources" on Android.
				This should not happen unless the Kotlin plugin now causes that to happen.
				Revision of build logic is thus necessary.
				""".trimIndent()
			)
		}
	}
}

private fun Project.setUpAssetsConvDummy(kotlin: KotlinMultiplatformExtension): String {
	val dummyDirProvider = layout.buildDirectory.dir("generated/assetsConvDummy")

	getSourceSets(kotlin).getByName("commonMain")
		.resources.srcDir(dummyDirProvider)

	@Suppress("UnstableApiUsage")
	return providers.of(AssetsConvDummyNameValueSource::class.java) {
		parameters.dummyDirProperty.set(dummyDirProvider)
	}.get()
}

@Suppress("UnstableApiUsage")
internal abstract class AssetsConvDummyNameValueSource :
	ValueSource<String, AssetsConvDummyNameValueSource.Parameters> {

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
