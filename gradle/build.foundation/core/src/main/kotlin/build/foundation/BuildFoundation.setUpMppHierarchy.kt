package build.foundation

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * @see mppHierarchy
 */
fun BuildFoundation.setUpMppHierarchy(project: Project) {
	if (project.extra.parseBoolean(extra__skipMppHierarchySetup, false)) return

	with(project.extensions.getByName("kotlin") as KotlinMultiplatformExtension) {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		applyHierarchyTemplate(mppHierarchy)
	}
}
