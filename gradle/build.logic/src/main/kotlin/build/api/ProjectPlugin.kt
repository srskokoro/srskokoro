package build.api

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @see org.gradle.api.Plugin
 */
interface ProjectPlugin : Plugin<Project> {

	override fun apply(project: Project)
}
