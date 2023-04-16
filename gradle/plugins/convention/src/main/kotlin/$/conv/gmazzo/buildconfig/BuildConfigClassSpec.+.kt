@file:Suppress("PackageDirectoryMismatch", "NOTHING_TO_INLINE")

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec

inline fun BuildConfigClassSpec.internalObject(name: String) =
	className(name).useKotlinOutput { internalVisibility = true }

inline fun BuildConfigClassSpec.internalObject(name: String, packageName: String) =
	internalObject(name).packageName(packageName)
