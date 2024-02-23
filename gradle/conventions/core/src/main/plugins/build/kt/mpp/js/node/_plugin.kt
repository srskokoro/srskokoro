package build.kt.mpp.js.node

import build.api.ProjectPlugin
import build.api.dsl.accessors.kotlinMpp
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp.js._plugin>()
	}

	(kotlinMpp.targets.findByName("js") as KotlinJsTargetDsl?)?.run {
		nodejs()
	}
})
