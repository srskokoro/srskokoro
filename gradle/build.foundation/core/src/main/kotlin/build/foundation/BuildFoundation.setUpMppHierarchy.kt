package build.foundation

import build.foundation.BuildFoundation.MPP
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun BuildFoundation.setUpMppHierarchy(project: Project): Unit = with(project) {
	with(extensions.getByName("kotlin") as KotlinMultiplatformExtension) {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		applyDefaultHierarchyTemplate {
			// Extend the default hierarchy with our own custom setup
			common {
				group(MPP.jvmish) {
					withAndroidTarget()
					withJvm()
				}
				group("native") {
					group(MPP.unix) {
						group("apple")
						group("linux")
						group("androidNative")
					}
				}
				group(MPP.desktop) {
					withJvm()
					group("linux")
					group("macos")
					group("mingw")
				}
				group(MPP.mobile) {
					withAndroidTarget()
					group("ios")
				}
			}
		}
	}
}
