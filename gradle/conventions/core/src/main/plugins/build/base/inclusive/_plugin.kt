package build.base.inclusive

import build.api.ProjectPlugin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpAsBuildInclusive
import org.gradle.kotlin.dsl.*

/**
 * @see build.root.inclusive._plugin
 */
class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpAsBuildInclusive(this)
})
