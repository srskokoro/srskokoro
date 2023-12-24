package build.support.kt.mpp

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.model.kotlinMpp
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin {

	override fun Project.applyPlugin() {
		apply {
			plugin(kotlin("multiplatform"))
			plugin<build.support.kt.base._plugin>()
		}

		kotlinMpp.run {
			// Should support at best targets that we can test with Kotest
			// (which is version 5.8.0 at the time of writing). See,
			// - https://github.com/kotest/kotest/blob/v5.8.0/buildSrc/src/main/kotlin/kotest-js-conventions.gradle.kts
			// - https://github.com/kotest/kotest/blob/v5.8.0/buildSrc/src/main/kotlin/kotest-native-conventions.gradle.kts

			js(IR) {
				browser()
				nodejs()
			}

			jvm()

			iosX64()
			iosArm64()
			iosSimulatorArm64()

			tvosX64()
			tvosArm64()
			tvosSimulatorArm64()

			watchosX64()
			watchosArm32()
			watchosArm64()
			watchosDeviceArm64()
			watchosSimulatorArm64()

			linuxX64()
			linuxArm64()

			macosX64()
			macosArm64()

			mingwX64()
		}
	}
}
