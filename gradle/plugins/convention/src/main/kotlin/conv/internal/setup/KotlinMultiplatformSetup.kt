package conv.internal.setup

import getAndroidAssets
import initAssetsAsResources
import initConvAssetsProcessingTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

internal fun Project.setUp(kotlin: KotlinMultiplatformExtension) {
	setUpAssetsDir(this, kotlin) // Must be done first, so that the following subsequent setup may see it.
	setUp(kotlin as KotlinProjectExtension)
}

/**
 * Provides the ability to have common assets, as "resources" in JVM, but as
 * "assets" in Android.
 */
private fun setUpAssetsDir(project: Project, kotlin: KotlinMultiplatformExtension) {
	val kotlinTargets = kotlin.targets
	kotlinTargets.withType<KotlinJvmTarget> {
		compilations.all {
			allKotlinSourceSets.forAll { sourceSet ->
				sourceSet.initAssetsAsResources(project)
			}
		}
	}
	project.ifAndroidProject {
		val android = project.androidExt
		kotlinTargets.withType<KotlinAndroidTarget> {
			compilations.all {
				allKotlinSourceSets.forAll { sourceSet ->
					sourceSet.initAssetsAsResources(project)
				}
				initConvAssetsProcessingTask()?.let { outputDir ->
					defaultSourceSet.getAndroidAssets(android)?.srcDir(outputDir)
				}
			}
		}
	}
}
