package build.plugins.testing.base

import build.api.ProjectPlugin
import build.api.dsl.accessors.api
import build.api.dsl.accessors.compileOnlyTestImpl
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("java-library")
		plugin("org.gradle.kotlin.kotlin-dsl.base")
		plugin<build.plugins.base.internal._plugin>()
	}

	dependencies.run {
		api("build:build.base")

		compileOnlyTestImpl(gradleTestKit())
		api(kotlin("test"))
	}
})
