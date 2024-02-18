package kokoro.build.kt.js

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinMpp
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.mpp._plugin>()
	}

	val kotlin = kotlinMpp

	@OptIn(InternalApi::class)
	if (BuildFoundation.shouldBuildJs(project)) {
		kotlin.run {
			js(IR) {
				browser()
			}
		}
	} else {
		BuildFoundation.registerMppDummyConfigurations("js", configurations)
		prioritizedAfterEvaluate {
			if (kotlin.targets.none { it !is KotlinMetadataTarget }) {
				// MPP must have at least one target initialized!
				kotlin.jvm("_KLUDGE_")
			}
		}
	}
})
