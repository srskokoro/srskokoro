package build.plugins

import build.api.ProjectPlugin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.contributesPlugins
import org.gradle.kotlin.dsl.*

/**
 * @see build.conventions._plugin
 */
class _plugin : ProjectPlugin({
	apply {
		plugin<build.plugins.support._plugin>()
		plugin("java-gradle-plugin")
	}

	@OptIn(InternalApi::class)
	BuildFoundation.contributesPlugins(this)
})
