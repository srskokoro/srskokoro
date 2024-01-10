import build.foundation.compileOnlyTestImpl
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal

plugins {
	`kotlin-dsl-base` apply false
	id("build.conventions.root")
}

allprojects(fun(project): Unit = with(project) {
	if (path == ":foojay") return // Exempted
	plugins.withId(build.conventions.support._plugin::class.java.packageName) {
		@OptIn(build.foundation.InternalApi::class)
		dependencies.run {
			// NOTE: The following will prevent `kotlin-stdlib` from being added
			// automatically by KGP -- see, https://kotlinlang.org/docs/gradle-configure-project.html#dependency-on-the-standard-library
			compileOnlyTestImpl(embeddedKotlin("stdlib"))
		}
		val cs = configurations
		cs.named("apiElements") { Build.ensureProjectOnlyDependencies(this, project) }
		cs.named("runtimeElements") { Build.ensureProjectOnlyDependencies(this, project) }
		// NOTE: Need to check transitive dependencies via `apiElements` and
		// `runtimeElements`, but they're both non-resolvable configurations.
		// Thus, let's just poke at `runtimeClasspath` instead.
		cs.named("runtimeClasspath", Build::ensureProjectOnlyDependenciesResolved)
	}
})

private object Build {
	const val ERROR_MESSAGE = "" +
		"Must only have `compileOnly()` or `project()` dependencies (or equivalent), to\n" +
		"avoid affecting the buildscript classpath of the consuming build."

	const val ERROR_MESSAGE_NOTES = "" +
		"Note that, `compileOnlyApi()` should not be used either. However, test source\n" +
		"sets should be free to have whatever dependencies."

	private fun Configuration.recursivelyRemoveDependency(dependency: Dependency) {
		dependencies.remove(dependency)
		extendsFrom.forEach { it.recursivelyRemoveDependency(dependency) }
	}

	fun ensureProjectOnlyDependencies(c: Configuration, from: Project) {
		from.afterEvaluate {
			// Remove the automatically added "Gradle API" dependency
			c.recursivelyRemoveDependency(dependencies.gradleApi())
		}
		// NOTE: We wrapped the following in `withDependencies()` because
		// `allDependencies` seems to not reflect further `extendsFrom()`
		// modifications.
		c.withDependencies(fun(_): Unit = c.allDependencies.configureEach(fun(d) {
			if (d !is ProjectDependency) {
				if (d.group == "build.foundation" && d.name == "core") {
					// It is an included build.
					return // Skip (silently)
				}
				val label: Any? = when (d) {
					is SelfResolvingDependencyInternal -> d.targetComponentId
					is ModuleVersionSelector -> d.module
					else -> d
				}
				error(ERROR_MESSAGE +
					"\n- Dependency: $label" +
					"\n- From: $from" +
					"\n$ERROR_MESSAGE_NOTES")
			}
		}))
	}

	fun ensureProjectOnlyDependenciesResolved(c: Configuration) = c.incoming.afterResolve {
		for (r in resolutionResult.allDependencies) if (r is ResolvedDependencyResult) {
			val id = r.selected.id
			if (id !is ProjectComponentIdentifier) {
				error(ERROR_MESSAGE +
					"\n- Dependency: $id" +
					"\n- From: ${r.from}" +
					"\n- Resolved via, $c" +
					"\n$ERROR_MESSAGE_NOTES")
			}
		}
	}
}
