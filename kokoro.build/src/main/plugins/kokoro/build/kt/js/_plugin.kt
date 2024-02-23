package kokoro.build.kt.js

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinMpp
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp.js.browser._plugin>()
	}

	val kotlin = kotlinMpp
	val kotlinTargets = kotlin.targets

	if (kotlinTargets.findByName("js") == null) {
		prioritizedAfterEvaluate {
			if (kotlinTargets.none { it !is KotlinMetadataTarget }) {
				// MPP must have at least one target initialized!
				kotlin.jvm("_KLUDGE_")
			}
		}
	}
})
