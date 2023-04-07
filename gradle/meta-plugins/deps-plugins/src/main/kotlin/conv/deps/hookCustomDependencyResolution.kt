package conv.deps

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.withType

/**
 * Provides a version for each plugin that has no version.
 */
internal fun hookCustomDependencyResolution(settings: Settings, map: Map<PluginId, Version>) {
	settings.pluginManagement.resolutionStrategy.eachPlugin {
		requested.run {
			if (version.isNullOrEmpty()) {
				id.let {
					map[PluginId.of(it)] ?: map[PluginId.ofAnyName(it)]
				}?.let { v ->
					useVersion(v.value)
				}
			}
		}
	}
}

/**
 * Provides a version for each "direct" dependency (i.e., non-transitive
 * dependency) that has no version.
 */
internal fun hookCustomDependencyResolution(project: Project, map: Map<ModuleId, Version>) {
	project.configurations.all {
		dependencies.withType(fun(dep: ExternalDependency) = dep.version {
			if (requiredVersion.isEmpty() && strictVersion.isEmpty()) {
				(map[ModuleId.of(dep)] ?: map[ModuleId.ofAnyName(dep)])?.let { v ->
					val rejectedVersionsBackup = rejectedVersions
						.takeUnless { it.isEmpty() }
						?.toTypedArray()

					require(v.value) // NOTE: Clears `rejectedVersions`

					rejectedVersionsBackup?.let {
						reject(*it)
					}
				}
			}
		})
	}
}
