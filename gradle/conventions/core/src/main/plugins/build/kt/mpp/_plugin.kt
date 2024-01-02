package build.kt.mpp

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.commonTestImplementation
import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import build.conventions.internal.setUpTestTasks
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("multiplatform"))
		plugin("io.kotest.multiplatform")
	}

	@OptIn(InternalConventionsApi::class)
	InternalConventions.setUpTestTasks(this)

	if (group != "multipurpose" && name != "testing") {
		dependencies.commonTestImplementation("multipurpose:testing")
	}
})
