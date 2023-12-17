package build.api

import org.gradle.api.Project

/**
 * @see org.gradle.api.Plugin
 */
abstract class ProjectPluginApply(
	apply: Project.() -> Unit,
) : ProjectPlugin, PluginApply<Project>(apply)
