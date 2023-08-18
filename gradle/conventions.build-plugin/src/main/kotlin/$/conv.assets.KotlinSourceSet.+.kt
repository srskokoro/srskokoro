@file:Suppress("PackageDirectoryMismatch")

import com.android.build.api.dsl.AndroidSourceDirectorySet
import com.android.build.api.dsl.AndroidSourceSet
import conv.internal.setup.*
import conv.internal.support.unsafeCast
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull

internal const val XS_assets = "assets"

val KotlinSourceSet.assets: SourceDirectorySet?
	get() = (this as ExtensionAware).extensions.findByName(XS_assets)?.unsafeCast()

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.getAndroidAssets(android: AndroidExtension): AndroidSourceDirectorySet? = getAndroidSourceSet(android)?.assets

fun KotlinSourceSet.getAndroidSourceSet(android: AndroidExtension): AndroidSourceSet? {
	@Suppress("UnstableApiUsage")
	@OptIn(ExperimentalKotlinGradlePluginApi::class)
	return androidSourceSetInfoOrNull?.let { info ->
		android.sourceSets.findByName(info.androidSourceSetName)
	}
}
