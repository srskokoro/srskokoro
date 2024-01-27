package build.kt.mpp.inclusive

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		// Must apply this first so that later plugins may see its presence
		plugin<build.base.inclusive._plugin>()
		plugin<build.kt.mpp.lib._plugin>()
	}

	kotlin.jvmToolchain { restrictVersionForBuildInclusive() }
})
