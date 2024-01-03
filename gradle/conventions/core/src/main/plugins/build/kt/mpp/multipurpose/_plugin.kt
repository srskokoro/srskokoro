package build.kt.mpp.multipurpose

import build.api.ProjectPlugin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.ensureMultipurpose
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp.lib._plugin>()
	}

	@OptIn(InternalApi::class)
	BuildFoundation.ensureMultipurpose(this)
})
