package build.kt.mpp.internal

import build.api.ProjectPlugin
import build.api.dsl.accessors.commonTestImplementation
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.internal._plugin>()
	}

	dependencies.run {
		commonTestImplementation(embeddedKotlin("test"))
	}
})
