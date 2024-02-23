package build.kt.mpp.js

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
	if (BuildFoundation.shouldBuildJs(this)) kotlinMpp.run {
		js(IR)
	} else {
		BuildFoundation.registerMppDummyConfigurations("js", configurations)
	}
})
