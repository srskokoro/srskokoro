package kokoro.build.kt.mpp.app

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinMpp
import build.foundation.BuildFoundation
import build.foundation.BuildFoundation.MPP
import build.foundation.InternalApi
import build.foundation.extendMppHierarchyTemplate
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

class _plugin : ProjectPlugin({

	@OptIn(InternalApi::class, ExperimentalKotlinGradlePluginApi::class)
	BuildFoundation.extendMppHierarchyTemplate(this) {
		common {
			group("host") {
				group(MPP.jvmish)
				group("native")
				group(MPP.desktop)
				group(MPP.mobile)
			}
		}
	}

	apply {
		plugin<kokoro.build.kt.mpp.internal._plugin>() // Will set up Android target automatically
	}

	val kotlin = kotlinMpp
	kotlin.run {
		js("ui", IR) {
			browser()
		}

		jvm()

		@OptIn(InternalApi::class)
		if (BuildFoundation.shouldBuildNative(projectThis)) {
			iosX64()
			iosArm64()
			iosSimulatorArm64()
		} else projectThis.configurations.run {
			registerMppDummyConfigurations("ios")
			registerMppDummyConfigurations("apple")
			registerMppDummyConfigurations(MPP.unix)
			registerMppDummyConfigurations("native")
		}
	}
})

private fun ConfigurationContainer.registerMppDummyConfigurations(name: String) {
	register("${name}MainApi")
	register("${name}MainImplementation")
	register("${name}MainCompileOnly")
	register("${name}MainRuntimeOnly")
	register("${name}TestApi")
	register("${name}TestImplementation")
	register("${name}TestCompileOnly")
	register("${name}TestRuntimeOnly")
}
