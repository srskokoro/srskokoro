package build.api.dsl

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.diagnostics.AbstractDependencyReportTask
import org.gradle.api.tasks.diagnostics.DependencyInsightReportTask
import org.gradle.kotlin.dsl.*

/**
 * Fails on transitive upgrade/downgrade of versions for dependencies under the
 * given [configuration] that are direct dependencies of any project component
 * (which isn't necessarily the current project).
 *
 * Useful for solving the issue described in “[Effects of Gradle's default resolution behavior | Understanding Gradle #10 – Dependency Version Conflicts](https://youtu.be/YYWhfy6c2YQ?t=145)”
 *
 * @param configuration A resolvable configuration.
 * @param enable `true` to enable; `false` to disable.
 */
fun Project.failOnDirectDependencyVersionGotcha(configuration: Configuration, enable: Boolean = true) {
	require(configuration.isCanBeResolved) {
		"Configuration must be resolvable: '$name'"
	}

	val extra = (configuration as ExtensionAware).extra
	val _failOnDirectDependencyVersionGotcha_isEnabled =
		"failOnDirectDependencyVersionGotcha_isEnabled"

	// Check if the extra property has ever been set (to any value) before.
	val alreadySetUpBefore = extra.has(_failOnDirectDependencyVersionGotcha_isEnabled)

	extra[_failOnDirectDependencyVersionGotcha_isEnabled] = enable

	if (alreadySetUpBefore) return

	@Suppress("UnstableApiUsage")
	val currentBuildPath = rootProject.buildTreePath

	configuration.incoming.afterResolve {
		if (extra[_failOnDirectDependencyVersionGotcha_isEnabled] != true) {
			return@afterResolve // Checks have been disabled
		}
		when (gradle.taskGraph.allTasks.lastOrNull()) {
			is AbstractDependencyReportTask,
			is DependencyInsightReportTask,
			-> return@afterResolve
		}
		this@afterResolve.doFailOnDirectDependencyVersionGotcha(currentBuildPath)
	}
}

private fun ResolvableDependencies.doFailOnDirectDependencyVersionGotcha(currentBuildPath: String) {
	val depSet = resolutionResult.allDependencies

	// NOTE: The reason why we must fail for dependencies of any project (and
	// not just the current project) is conveyed by the following scenario:
	//
	// Suppose project A depends on a certain version of dependency X, and
	// project B depends on a certain dependency that depends on a different
	// version of dependency X; if a project depends on both project A and
	// project B, then a failure should occur if project A gets its version of
	// dependency X upgraded by project B's dependency.
	// --

	// Cache for selected versions directly requested by project components
	val reqByAnyProj = mutableSetOf<Triple<ProjectComponentIdentifier, ModuleIdentifier, String>>()

	// Cache for selected components whose dependents we've already scanned
	val selDependentsScanned = mutableSetOf<ResolvedComponentResult>()

	// Resolved dependencies that failed our check criteria
	val failedSet = mutableSetOf<ResolvedDependencyResult>()

	for (dep in depSet) {
		if (dep !is ResolvedDependencyResult) continue

		// Include only dependencies directly declared by project components
		val projectId = dep.from.id as? ProjectComponentIdentifier ?: continue

		// Exclude those from projects that are not from the current build --
		// i.e., treat the dependency like it was contributed externally.
		if (projectId.build.buildPath != currentBuildPath) continue

		val req = dep.requested
		if (req !is ModuleComponentSelector) continue

		@Suppress("RedundantNullableReturnType", "RedundantSuppression")
		val reqVer: String? = req.version
		if (reqVer.isNullOrBlank()) continue

		// --

		val sel = dep.selected
		val selModVer = sel.moduleVersion ?: continue

		val selVer = selModVer.version
		if (reqVer == selVer) continue

		val selModId = selModVer.module
		if (selDependentsScanned.add(sel)) for (dependent in sel.dependents) {
			val dependentProjectId = dependent.from.id as? ProjectComponentIdentifier ?: continue
			val dependentReqVer = (dependent.requested as? ModuleComponentSelector ?: continue).version

			// Cache to speed up future checks
			reqByAnyProj += Triple(dependentProjectId, selModId, dependentReqVer)
		}

		// Allow the requested version to be changed by a project component
		// (into the selected version), so long as the change was done by the
		// same project that directly requested for the differing version.
		if (Triple(projectId, selModId, selVer) in reqByAnyProj) continue
		// ^ Remember, `projectId` is the ID of the project that directly
		// requested for the dependency, while `selVer` is the actual version
		// selected despite what the project requested.

		if (!sel.selectionReason.isConflictResolution) continue

		failedSet.add(dep)
	}

	if (failedSet.isNotEmpty()) throw GradleException(
		"""
		Could not resolve all dependencies for configuration '$name'.
		Conflict(s) found for the following direct dependencies of project components:
		""".trimIndent() + failedSet.joinToString("", prefix = "\n") {
			"""
			- $it
			  - from '${it.from}'
			  - changed by '${it.selected.findFirstDependentWithMatchingVersion().from}'
			""".replaceIndent("  ") + "\n"
		} + """
		Note:
		  For each of the above, the version has been quietly changed by a transitive
		  dependency, but the configuration's resolution strategy has forbidden this for
		  all direct dependencies of project components.
		--
		""".trimIndent()
	)
}

private fun ResolvedComponentResult.findFirstDependentWithMatchingVersion(): ResolvedDependencyResult =
	dependents.first { (it.requested as? ModuleComponentSelector)?.version == this.moduleVersion?.version }
