package build.foundation

import build.foundation.BuildFoundation.MPP
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.extend

/**
 * @see BuildFoundation.setUpMppHierarchy
 * @see BuildFoundation.extra__skipMppHierarchySetup
 */
val BuildFoundation.mppHierarchy get() = mppHierarchy_

@OptIn(ExperimentalKotlinGradlePluginApi::class)
private val mppHierarchy_ = KotlinHierarchyTemplate.default.extend {
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
