import conv.internal.setup.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/**
 * Provides the ability to have common assets, as "resources" in JVM, but as
 * "assets" in Android.
 */
run<Unit> {
	var isSupported = false
	afterEvaluate {
		check(isSupported)
	}
	plugins.withId("org.jetbrains.kotlin.multiplatform") {
		isSupported = true

		val kotlin = extensions.getByName("kotlin") as KotlinMultiplatformExtension
		val kotlinTargets = kotlin.targets

		kotlinTargets.withType<KotlinJvmTarget> {
			compilations.all {
				allKotlinSourceSets.forAll { sourceSet ->
					sourceSet.initAssetsAsResources(project)
				}
			}
		}

		ifAndroidProject {
			val android = androidExt
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
}
