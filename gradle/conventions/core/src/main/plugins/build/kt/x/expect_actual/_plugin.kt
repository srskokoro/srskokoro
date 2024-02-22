package build.kt.x.expect_actual

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

class _plugin : ProjectPlugin({
	tasks.withType<KotlinCompile<*>>().configureEach {
		// See, https://youtrack.jetbrains.com/issue/KT-61573
		kotlinOptions.options.freeCompilerArgs.add("-Xexpect-actual-classes")
	}
})
