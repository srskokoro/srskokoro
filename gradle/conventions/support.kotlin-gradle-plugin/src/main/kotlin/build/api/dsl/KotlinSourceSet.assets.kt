package build.api.dsl

import build.InternalApi
import com.android.build.api.dsl.AndroidSourceDirectorySet
import com.android.build.api.dsl.AndroidSourceSet
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.android.KotlinAndroidSourceSetInfo
import org.jetbrains.kotlin.gradle.plugin.sources.android.androidSourceSetInfoOrNull

@InternalApi
const val assets__extension = "assets"

val KotlinSourceSet.assets: SourceDirectorySet?
	@OptIn(InternalApi::class)
	get() = xs().getSafelyOrNull(assets__extension)

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
	return android.sourceSets.findByName(androidSourceSetName)
}
