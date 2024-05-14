package org.gradle.kotlin.dsl

import build.api.dsl.*
import build.dependencies.Deps
import org.gradle.api.Action
import org.gradle.api.Project

// Named like this to discourage direct access
private const val deps__key = "--deps--"

val Project.deps: Deps?
	// NOTE: Given that the extension below is set up via the `settings` plugin,
	// it will be null if Gradle simply evaluated a fake project in order to
	// generate type-safe model accessors.
	get() = rootProject.xs().getOrNull(deps__key)

// NOTE: Correct line numbers are reported only when `Action<T>` is used --
// i.e., we can't even use `() -> Unit` here (`inline` or not).
// - This restriction seem to apply only to calls made directly at the top-level
// of the kotlin script (`*.kts`) file.
fun Project.deps(configure: Action<Deps>) {
	deps?.let { configure.execute(it) }
}

internal fun setUpDeps(rootProject: Project, deps: Deps) {
	rootProject.xs().add(typeOf(), deps__key, deps)
}
