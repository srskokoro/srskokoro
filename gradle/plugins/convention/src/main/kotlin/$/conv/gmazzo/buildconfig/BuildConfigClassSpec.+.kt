@file:Suppress("PackageDirectoryMismatch", "NOTHING_TO_INLINE")

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec

inline infix fun BuildConfigClassSpec.inPackage(packageName: String) = packageName(packageName)

inline fun BuildConfigClassSpec.internalObject(name: String) =
	className(name).useKotlinOutput { internalVisibility = true }
