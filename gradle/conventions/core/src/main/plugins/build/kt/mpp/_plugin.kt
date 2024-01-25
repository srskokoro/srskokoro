package build.kt.mpp

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.commonTestImplementation
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpMppHierarchy
import build.setUp
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("multiplatform"))
		plugin("io.kotest.multiplatform")
	}

	setUp(this)

	onAndroid {
		setUp(this)
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpMppHierarchy(this)

	if (group != "inclusives" && name != "testing") {
		dependencies.commonTestImplementation("inclusives:testing")
	}
})
