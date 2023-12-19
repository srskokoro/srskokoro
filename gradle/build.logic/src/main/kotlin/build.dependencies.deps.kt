import build.api.dsl.*
import build.dependencies.Deps
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

// Named like this to discourage direct access
private const val deps__key = "--deps--"

val Project.deps: Deps?
	get() = rootProject.xs().getOrNull(deps__key)

internal fun setUpDeps(rootProject: Project, deps: Deps) {
	rootProject.xs().add(typeOf(), deps__key, deps)
}
