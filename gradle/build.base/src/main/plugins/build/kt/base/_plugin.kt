package build.kt.base

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlin
import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

class _plugin : ProjectPlugin({
	plugins.withType<KotlinBasePluginWrapper> {
		project().xs().add("kotlinSourceSets", kotlin.kotlinSourceSets)
	}
})
