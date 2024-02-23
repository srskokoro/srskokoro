package kokoro.build.kt.js.packed

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinMpp
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

internal const val JS_PACKED = "jsPacked"

class _plugin : ProjectPlugin({
	extra["kotlin.js.ir.output.granularity"] = "whole-program"

	apply {
		plugin<kokoro.build.kt.js._plugin>()
	}

	(kotlinMpp.targets.findByName("js") as KotlinJsTargetDsl?)?.let { js ->
		js.binaries.executable()

		// See also, https://kotlinlang.org/docs/js-project-setup.html#building-executables
		val webpack = tasks.named<KotlinWebpack>("jsBrowserProductionWebpack")

		configurations.channelOutgoing(JS_PACKED).outgoing.run {
			artifact(webpack.map { t -> t.outputDirectory.file(t.mainOutputFileName) })
			artifact(webpack.map { t -> t.outputDirectory.file(t.mainOutputFileName.map { "$it.map" }) })
		}
	}
})
