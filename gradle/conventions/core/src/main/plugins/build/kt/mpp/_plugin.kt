package build.kt.mpp

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.commonTestImplementation
import build.api.dsl.accessors.kotlinMpp
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpMppHierarchy
import build.setUp
import build.setUpForAndroid
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("multiplatform"))
		plugin("io.kotest.multiplatform")
	}

	setUp(this)

	val kotlin = kotlinMpp
	setUpAssetsDirs(kotlin)

	onAndroid {
		setUp(this)
		setUpForAndroid(kotlin)
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpMppHierarchy(this)

	if (group != "inclusives" && name != "testing") {
		dependencies.commonTestImplementation("inclusives:testing")
	}
})
