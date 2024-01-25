package build.foundation

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * @see defaultMppHierarchyTemplate
 * @see getMppHierarchyTemplate
 * @see setMppHierarchyTemplate
 * @see extendMppHierarchyTemplate
 */
fun BuildFoundation.setUpMppHierarchy(project: Project) {
	project.extensions.add<Any>(mppHierarchyTemplate_applied__extension, true)

	with(project.extensions.getByName("kotlin") as KotlinMultiplatformExtension) {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		applyHierarchyTemplate(getMppHierarchyTemplate(project))
	}
}
