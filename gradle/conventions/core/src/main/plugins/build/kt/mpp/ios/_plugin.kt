package build.kt.mpp.ios

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	@OptIn(InternalApi::class)
	if (BuildFoundation.shouldBuildNative(this)) kotlinMpp.run {
		iosX64()
		iosArm64()
		iosSimulatorArm64()
	} else {
		BuildFoundation.registerMppDummyConfigurations("ios", configurations)
	}
})
