@file:Suppress("PackageDirectoryMismatch")

import build.deps.DependencyVersionsSetup
import build.deps.spec.DependencyVersionsSpec
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create

/**
 * NOTE: Can't use `::`[dependencyVersions]`.name` due to overload ambiguity. So
 * we created this constant.
 */
private const val dependencyVersions__name = "dependencyVersions"

val Settings.dependencyVersions: DependencyVersionsSpec
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	get() = extensions.findByName(dependencyVersions__name) as DependencyVersionsSpec? ?: failOnNullDependencyVersions()

internal fun Settings.ensureDependencyVersions() = extensions.let {
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	it.findByName(dependencyVersions__name) as DependencyVersionsSpec?
	?: it.create(dependencyVersions__name, this)
}

fun Settings.dependencyVersions(config: DependencyVersionsSpec.() -> Unit) {
	// May throw. Thus, we can't be `inline` (or Gradle will report incorrect line numbers :P)
	val spec = dependencyVersions
	spec.config()
}


private fun failOnNullDependencyVersions(): Nothing = throw IllegalStateException(
	"""
	Must first call either ${
		"`${DependencyVersionsSetup::export.name}()` or " +
		"`${DependencyVersionsSetup::useInProjects.name}()` via " +
		"`$dependencyVersionsSetup__name`"
	}
	""".trimIndent()
)
