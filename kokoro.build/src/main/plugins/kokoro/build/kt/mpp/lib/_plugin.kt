package kokoro.build.kt.mpp.lib

import build.api.ProjectPlugin
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<kokoro.build.kt.mpp.internal._plugin>()
	}
})
