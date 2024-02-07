@file:Suppress("NOTHING_TO_INLINE")

package org.gradle.kotlin.dsl

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator

inline infix fun <T : BuildConfigClassSpec> T.inPackage(packageName: String): T {
	this.packageName.set(packageName)
	return this
}

@PublishedApi
internal fun BuildConfigSourceSet.useKotlinOutput_(isTopLevel: Boolean, isInternal: Boolean) {
	generator.set(BuildConfigKotlinGenerator(topLevelConstants = isTopLevel, internalVisibility = isInternal))
}

inline fun <T : BuildConfigSourceSet> T.asObject(isInternal: Boolean = true): T {
	useKotlinOutput_(isTopLevel = false, isInternal = isInternal)
	return this
}

inline fun <T : BuildConfigSourceSet> T.asTopLevel(isInternal: Boolean = true): T {
	useKotlinOutput_(isTopLevel = true, isInternal = isInternal)
	return this
}

inline fun <T : BuildConfigSourceSet> T.asObject(className: String, isInternal: Boolean = true): T {
	this.className.set(className)
	return asObject(isInternal = isInternal)
}

inline fun <T : BuildConfigSourceSet> T.asTopLevel(className: String, isInternal: Boolean = true): T {
	this.className.set(className)
	return asTopLevel(isInternal = isInternal)
}

// --

inline fun <T : BuildConfigSourceSet> T.asPublicObject(): T = asObject(isInternal = false)

inline fun <T : BuildConfigSourceSet> T.asPublicTopLevel(): T = asTopLevel(isInternal = false)

inline fun <T : BuildConfigSourceSet> T.asPublicObject(className: String): T = asObject(className, isInternal = false)

inline fun <T : BuildConfigSourceSet> T.asPublicTopLevel(className: String): T = asTopLevel(className, isInternal = false)
