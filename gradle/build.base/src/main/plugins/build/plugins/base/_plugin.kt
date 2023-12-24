package build.plugins.base

import build.api.ProjectPlugin
import build.api.dsl.accessors.api
import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("java-gradle-plugin")
		plugin("org.gradle.kotlin.kotlin-dsl.base")
		plugin<build.kt.jvm.base._plugin>()
		plugin<build.support.kt.base._plugin>()
	}

	kotlinSourceSets.named("main", ::installPluginsAutoRegistrant)

	dependencies.run {
		api("build:build.base")
	}
})
