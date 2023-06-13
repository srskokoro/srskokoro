@file:Suppress("PackageDirectoryMismatch")

import com.android.build.api.dsl.AndroidSourceDirectorySet
import com.android.build.api.dsl.AndroidSourceSet
import conv.internal.setup.*
import conv.internal.support.unsafeCast
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.add
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull
import java.util.concurrent.Callable

val KotlinSourceSet.assets: SourceDirectorySet?
	get() = (this as ExtensionAware).extensions.findByName(::assets.name)?.unsafeCast()

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.getAndroidAssets(android: AndroidExtension): AndroidSourceDirectorySet? = getAndroidSourceSet(android)?.assets

fun KotlinSourceSet.getAndroidSourceSet(android: AndroidExtension): AndroidSourceSet? {
	@Suppress("UnstableApiUsage")
	@OptIn(ExperimentalKotlinGradlePluginApi::class)
	return androidSourceSetInfoOrNull?.let { info ->
		android.sourceSets.findByName(info.androidSourceSetName)
	}
}

// --

internal fun KotlinSourceSet.initAssetsAsResources(project: Project) {
	@Suppress("OPT_IN_USAGE")
	if (androidSourceSetInfoOrNull != null) return // Skip (for Android)

	val extensions = (this as ExtensionAware).extensions
	if (extensions.findByName(this::assets.name) != null) return // Skip. Already defined.

	val assetsDisplayName = "$name assets (conv)"
	val assets = project.objects.sourceDirectorySet(assetsDisplayName, assetsDisplayName)

	extensions.add<SourceDirectorySet>(this::assets.name, assets)
	assets.srcDir(project.file("src/${this.name}/assets"))

	// Set up as an additional resources directory of the current source set
	resources.srcDir(assets)
}

internal fun KotlinJvmAndroidCompilation.initConvAssetsProcessingTask(): Provider<Directory>? {
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
