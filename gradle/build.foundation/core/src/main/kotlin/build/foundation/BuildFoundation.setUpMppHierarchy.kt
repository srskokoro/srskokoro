package build.foundation

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun BuildFoundation.setUpMppHierarchy(project: Project): Unit = with(project) {
	with(extensions.getByName("kotlin") as KotlinMultiplatformExtension) {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		applyHierarchyTemplate(mppHierarchy)
	}
}
