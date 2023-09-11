@file:Suppress("PackageDirectoryMismatch")

import com.android.build.api.dsl.AndroidSourceDirectorySet
import com.android.build.api.dsl.AndroidSourceSet
import conv.internal.setup.*
import conv.internal.support.unsafeCast
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.android.KotlinAndroidSourceSetInfo
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull

internal const val XS_assetsConv = "assetsConv"

val KotlinSourceSet.assets: SourceDirectorySet?
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	get() = (this as ExtensionAware).extensions.findByName(XS_assetsConv).unsafeCast<SourceDirectorySet?>()

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.getAndroidAssets(android: AndroidExtension): AndroidSourceDirectorySet? = getAndroidSourceSet(android)?.assets

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinAndroidSourceSetInfo.getAndroidAssets(android: AndroidExtension): AndroidSourceDirectorySet? = getAndroidSourceSet(android)?.assets

fun KotlinSourceSet.getAndroidSourceSet(android: AndroidExtension): AndroidSourceSet? {
	@OptIn(ExperimentalKotlinGradlePluginApi::class)
	return androidSourceSetInfoOrNull?.getAndroidSourceSet(android)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinAndroidSourceSetInfo.getAndroidSourceSet(android: AndroidExtension): AndroidSourceSet? {
	@Suppress("UnstableApiUsage")
	return android.sourceSets.findByName(androidSourceSetName)
}
