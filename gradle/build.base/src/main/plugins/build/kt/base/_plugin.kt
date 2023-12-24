package build.kt.base

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlin
import build.api.dsl.accessors.kotlinSourceSets

class _plugin : ProjectPlugin({
	xs().add("kotlinSourceSets", kotlin.kotlinSourceSets)
})
