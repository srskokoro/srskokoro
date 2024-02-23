package build.kt.mpp.lib

import build.api.ProjectPlugin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.ensureMppHierarchyTemplateDefaultNodes
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()

		plugin<build.kt.mpp.jre._plugin>()

		plugin<build.kt.mpp.js.browser._plugin>()
		plugin<build.kt.mpp.js.node._plugin>()

		plugin<build.kt.mpp.ios._plugin>()
	}

	@OptIn(InternalApi::class)
	BuildFoundation.ensureMppHierarchyTemplateDefaultNodes(this)
})
