@file:Suppress("PackageDirectoryMismatch", "NOTHING_TO_INLINE")

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec

inline infix fun BuildConfigClassSpec.inPackage(packageName: String) = packageName(packageName)

inline fun BuildConfigClassSpec.internalObject(name: String) =
	className(name).useKotlinOutput { internalVisibility = true }

inline fun BuildConfigClassSpec.publicObject(name: String) =
	className(name).useKotlinOutput()

inline fun BuildConfigClassSpec.internalTopLevel(name: String? = null) = run {
	className.set(name)
	useKotlinOutput { topLevelConstants = true; internalVisibility = true }
}

inline fun BuildConfigClassSpec.publicTopLevel(name: String? = null) = run {
	className.set(name)
	useKotlinOutput { topLevelConstants = true }
}
