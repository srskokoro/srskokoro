package kokoro.build.kt.mpp.app

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<kokoro.build.kt.mpp.internal._plugin>()
	}
})
