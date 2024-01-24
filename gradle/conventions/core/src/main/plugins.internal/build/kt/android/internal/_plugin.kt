package build.kt.android.internal

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.android
import build.api.dsl.accessors.testImplementation
import build.setUp
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("android"))
	}

	setUp(this)
	setUp(android)

	if (group != "inclusives" && name != "testing") {
		dependencies.testImplementation("inclusives:testing")
	}
})
