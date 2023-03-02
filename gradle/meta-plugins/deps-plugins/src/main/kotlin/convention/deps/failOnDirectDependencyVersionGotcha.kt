@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.diagnostics.AbstractDependencyReportTask
import org.gradle.api.tasks.diagnostics.DependencyInsightReportTask
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate

/**
 * Fails on transitive upgrade/downgrade of versions for dependencies directly
 * declared under this configuration.
 *
 * Useful for solving the issue described in “[Effects of Gradle's default resolution behavior | Understanding Gradle #10 – Dependency Version Conflicts](https://youtu.be/YYWhfy6c2YQ?t=145)”
 *
 * @receiver A resolvable configuration.
 * @param gradle Non-null to enable. Null to disable.
 */
fun Configuration.failOnDirectDependencyVersionGotcha(gradle: Gradle?) =
	failOnDirectDependencyVersionGotcha(gradle) { false }

/**
 * See, [failOnDirectDependencyVersionGotcha]
 *
 * @param gradle Non-null to enable. Null to disable.
 * @param excludeAlreadyDeclared If `true`, dependencies already declared when
 * this method is called will be excluded from the check.
 */
fun Configuration.failOnDirectDependencyVersionGotcha(gradle: Gradle?, excludeAlreadyDeclared: Boolean) {
	val excludeFilter: (ModuleIdentifier) -> Boolean = if (!excludeAlreadyDeclared) {
		({ false })
	} else {
		val alreadyDeclared: Set<Pair<String, String>> = allDependencies
			.mapNotNullTo(mutableSetOf()) { it.group?.let { g -> g to it.name } }

		({ it.group to it.name in alreadyDeclared })
	}
	return failOnDirectDependencyVersionGotcha(gradle, excludeFilter)
}

/**
 * See, [failOnDirectDependencyVersionGotcha]
 *
 * @param gradle Non-null to enable. Null to disable.
 * @param excludeFilter A filter over the direct dependencies under this
 * configuration; it must return `true` if a dependency should be excluded from
 * the check.
 */
fun Configuration.failOnDirectDependencyVersionGotcha(
	gradle: Gradle? = null,
	excludeFilter: (ModuleIdentifier) -> Boolean
) {
	require(isCanBeResolved) {
		"Configuration must be resolvable: '$name'"
	}

	val extra = (this as ExtensionAware).extra
	val _failOnDirectDependencyVersionGotcha_isEnabled =
		"failOnDirectDependencyVersionGotcha_isEnabled"

	val alreadySetUpBefore = extra.has(_failOnDirectDependencyVersionGotcha_isEnabled)

	// Non-null `gradle` instance to enable. Null to disable.
	extra[_failOnDirectDependencyVersionGotcha_isEnabled] = gradle != null

	var failOnDirectDependencyVersionGotcha_excludeFilter: (ModuleIdentifier) -> Boolean by extra
	failOnDirectDependencyVersionGotcha_excludeFilter = excludeFilter

	if (alreadySetUpBefore) return

	incoming.afterResolve {
		if (extra[_failOnDirectDependencyVersionGotcha_isEnabled] != true) {
			return@afterResolve // Checks have been disabled
		}
		if (gradle != null) when (gradle.taskGraph.allTasks.lastOrNull()) {
			is AbstractDependencyReportTask,
			is DependencyInsightReportTask,
			-> return@afterResolve
		}
		this.doFailOnDirectDependencyVersionGotcha(failOnDirectDependencyVersionGotcha_excludeFilter)
	}
}

private fun ResolvableDependencies.doFailOnDirectDependencyVersionGotcha(excludeFilter: (ModuleIdentifier) -> Boolean) {
	val depSet = resolutionResult.root.dependencies
	val reqModVerSet = mutableSetOf<Pair<ModuleIdentifier, String>>()

	// Gather the versions first, as some dependencies could be declared more
	// than once under different versions.
	for (dep in depSet) {
		val req = dep.requested
		if (req !is ModuleComponentSelector) continue

		val reqVer = req.version
		if (reqVer.isNullOrBlank()) continue

		val reqModId = req.moduleIdentifier
		reqModVerSet.add(reqModId to reqVer)
	}

	// Look for dependencies that failed our check criteria.
	val failedSet = mutableSetOf<ResolvedDependencyResult>()
	for (dep in depSet) {
		if (dep !is ResolvedDependencyResult) continue

		val req = dep.requested
		if (req !is ModuleComponentSelector) continue

		val reqVer = req.version
		if (reqVer.isNullOrBlank()) continue

		// --

		val sel = dep.selected
		val selModVer = sel.moduleVersion ?: continue

		val selModId = selModVer.module
		if (selModId to selModVer.version in reqModVerSet) continue

		if (!sel.selectionReason.isConflictResolution) continue
		if (excludeFilter(selModId)) continue

		failedSet.add(dep)
	}

	if (failedSet.isNotEmpty()) throw GradleException(
		"""
		Conflicts found for the following direct dependencies of configuration '$name':
		""".trimIndent() +
		"\n" + failedSet.joinToString("") { "  - $it\n" } +
		"""
		Note:
		  For each of the above, the version has been quietly changed by a transitive
		  dependency, but the resolution strategy has forbidden this.
		--
		""".trimIndent()
	)
}
