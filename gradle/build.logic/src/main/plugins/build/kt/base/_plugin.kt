package build.kt.base

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.model.kotlin
import build.api.dsl.model.kotlinSourceSets
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

class _plugin : ProjectPlugin {
	override fun Project.applyPlugin() {
		plugins.withType<KotlinBasePluginWrapper> {
			project().xs().add("kotlinSourceSets", kotlin.kotlinSourceSets)
		}
	}
}
