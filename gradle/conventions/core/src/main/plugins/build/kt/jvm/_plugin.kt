package build.kt.jvm

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.testImplementation
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpTestTasks
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("jvm"))
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpTestTasks(this)

	if (group != "multipurpose" && name != "testing") {
		dependencies.testImplementation("multipurpose:testing")
	}
})
