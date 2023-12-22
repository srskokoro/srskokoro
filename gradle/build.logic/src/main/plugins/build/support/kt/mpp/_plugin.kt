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
			js(IR) {
				nodejs()
			}

			jvm()

			iosArm64()
			iosSimulatorArm64()
			iosX64()

			linuxX64()
			linuxArm64()
			macosX64()
			macosArm64()
			mingwX64()
		}
	}
}
