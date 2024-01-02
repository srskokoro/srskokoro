package build.kt.jvm

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.testImplementation
import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import build.conventions.internal.setUpTestTasks
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("jvm"))
	}

	@OptIn(InternalConventionsApi::class)
	InternalConventions.setUpTestTasks(this)

	if (group != "multipurpose" && name != "testing") {
		dependencies.testImplementation("multipurpose:testing")
	}
})
