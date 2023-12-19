package build.dependencies

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.*

/**
 * Provides a version for each plugin that has no version.
 */
internal fun hookCustomDependencyResolution(settings: Settings, map: Map<PluginId, String>) {
	settings.pluginManagement.resolutionStrategy.eachPlugin {
		requested.run {
			if (version.isNullOrEmpty()) {
				val v = map[PluginId.of_unsafe(id.id)]
				if (v != null) useVersion(v)
			}
		}
	}
}

/**
 * Provides a version for each "direct" dependency (i.e., non-transitive
 * dependency) that has no version.
 */
internal fun hookCustomDependencyResolution(project: Project, map: Map<ModuleId, String>) {
	project.configurations.configureEach {
		dependencies.withType<ExternalDependency>().configureEach(fun(dep) = dep.version {
			if (requiredVersion.isEmpty() && strictVersion.isEmpty()) {
				val v = map[ModuleId.of_unsafe(dep.group, dep.name)]
				if (v != null) {
					val rejectedVersionsBackup = rejectedVersions.let {
						if (it.isEmpty()) null else it.toTypedArray()
					}
					require(v) // NOTE: Clears `rejectedVersions`
					rejectedVersionsBackup?.let { reject(*it) }
				}
			}
		})
	}
}
