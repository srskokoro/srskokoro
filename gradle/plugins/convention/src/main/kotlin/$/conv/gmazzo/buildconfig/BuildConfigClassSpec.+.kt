@file:Suppress("PackageDirectoryMismatch", "NOTHING_TO_INLINE")

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

inline infix fun BuildConfigClassSpec.inPackage(packageName: String) = packageName(packageName)

inline fun BuildConfigSourceSet.internalObject(name: String) = run {
	className.set(name)
	useKotlinOutput()
}

inline fun BuildConfigSourceSet.publicObject(name: String) = run {
	className.set(name)
	useKotlinOutput { internalVisibility = false }
}

inline fun BuildConfigSourceSet.internalTopLevel(name: String? = null) = run {
	className.set(name)
	useKotlinOutput { topLevelConstants = true }
}

inline fun BuildConfigSourceSet.publicTopLevel(name: String? = null) = run {
	className.set(name)
	useKotlinOutput { topLevelConstants = true; internalVisibility = false }
}
