package build.api

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @see org.gradle.api.Plugin
 */
abstract class ProjectPlugin(
	private val apply: Project.() -> Unit = {},
) : Plugin<Project> {
	override fun apply(target: Project) {
		apply.invoke(target)
	}
}
