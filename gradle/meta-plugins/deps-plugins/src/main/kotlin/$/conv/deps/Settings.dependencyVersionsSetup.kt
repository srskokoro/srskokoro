@file:Suppress("PackageDirectoryMismatch")

import conv.deps.DependencyVersionsSetup
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create

/**
 * NOTE: Can't use `::`[dependencyVersionsSetup]`.name` due to overload
 * ambiguity. So we created this constant.
 */
internal const val dependencyVersionsSetup__name = "dependencyVersionsSetup"

val Settings.dependencyVersionsSetup
	get() = extensions.getByName(dependencyVersionsSetup__name) as DependencyVersionsSetup

internal fun Settings.installDependencyVersionsSetup() {
	extensions.create<DependencyVersionsSetup>(dependencyVersionsSetup__name, this)
}

inline fun Settings.dependencyVersionsSetup(
	crossinline config: DependencyVersionsSetup.() -> Unit
) = dependencyVersionsSetup.config()
