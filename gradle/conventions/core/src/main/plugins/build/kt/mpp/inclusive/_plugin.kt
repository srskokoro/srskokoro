package build.kt.mpp.inclusive

import build.api.ProjectPlugin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpAsBuildInclusive
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp.lib._plugin>()
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpAsBuildInclusive(this)
})
