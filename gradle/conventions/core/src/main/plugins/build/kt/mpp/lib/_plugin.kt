package build.kt.mpp.lib

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	kotlinMpp.run {
		// Should support at best, targets that we can test with Kotest
		// (which is version 5.8 at the time of writing). See,
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
})
