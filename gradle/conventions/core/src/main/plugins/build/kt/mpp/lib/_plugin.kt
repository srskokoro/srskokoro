package build.kt.mpp.lib

import build.api.ProjectPlugin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpMppLibTargets
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpMppLibTargets(this)
})
