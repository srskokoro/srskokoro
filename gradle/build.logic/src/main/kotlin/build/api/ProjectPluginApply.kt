package build.api

import org.gradle.api.Project

/**
 * @see org.gradle.api.Plugin
 */
abstract class ProjectPluginApply(
	apply: Project.() -> Unit,
) : ProjectPlugin, PluginApply<Project>(apply) {

	@Suppress("RedundantOverride", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
	override fun apply(project: Project) = super.apply(project)
}
