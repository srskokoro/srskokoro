package build.kt.mpp

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.commonTestImplementation
import build.api.dsl.accessors.kotlin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpTestTasks
import build.setUpAltSrcDirs
import build.setUpJvmToolchain
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("multiplatform"))
		plugin("io.kotest.multiplatform")
	}

	setUpJvmToolchain(kotlin)
	setUpAltSrcDirs()

	@OptIn(InternalApi::class)
	BuildFoundation.setUpTestTasks(this)

	if (group != "inclusives" && name != "testing") {
		dependencies.commonTestImplementation("inclusives:testing")
	}
})
