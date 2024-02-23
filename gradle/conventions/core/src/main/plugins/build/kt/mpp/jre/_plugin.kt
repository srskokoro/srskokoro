package build.kt.mpp.jre

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import build.foundation.BuildFoundation.MPP
import build.foundation.InternalApi
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	@OptIn(InternalApi::class)
	kotlinMpp.run {
		jvm(MPP.jre)
	}
})
