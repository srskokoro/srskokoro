package build.plugins

import build.api.ProjectPlugin
import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import build.conventions.internal.contributesPlugins
import org.gradle.kotlin.dsl.*

/**
 * @see build.conventions._plugin
 */
class _plugin : ProjectPlugin({
	apply {
		plugin<build.plugins.support._plugin>()
		plugin("java-gradle-plugin")
	}

	@OptIn(InternalConventionsApi::class)
	InternalConventions.contributesPlugins(this)
})
