package build.foundation

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun BuildFoundation.setUpMppLibTargets(project: Project): Unit = with(project) {
	with(extensions.getByName("kotlin") as KotlinMultiplatformExtension) {
		// Apply this now (instead of waiting for it to be applied later), so
		// that Gradle may generate type-safe model accessors for the default
		// hierarchy.
		applyDefaultHierarchyTemplate()

		// -=-
		// Should support at best, targets that we can test with Kotest
		// (which is version 5.8 at the time of writing). See,
		// - https://github.com/kotest/kotest/blob/v5.8.0/buildSrc/src/main/kotlin/kotest-js-conventions.gradle.kts
		// - https://github.com/kotest/kotest/blob/v5.8.0/buildSrc/src/main/kotlin/kotest-native-conventions.gradle.kts

		jvm()

		if (project.extra.parseBoolean("BUILD_KJS", true)) {
			js(IR) {
				browser()
				nodejs()
			}
		}

		if (project.extra.parseBoolean("BUILD_KN", true)) {
			iosX64()
			iosArm64()
			iosSimulatorArm64()

			tvosX64()
			tvosArm64()
			tvosSimulatorArm64()

			watchosX64()
			watchosArm32()
			watchosArm64()
//			watchosDeviceArm64()
			watchosSimulatorArm64()

			linuxX64()
			linuxArm64()

			macosX64()
			macosArm64()

			mingwX64()
		}
	}
}
