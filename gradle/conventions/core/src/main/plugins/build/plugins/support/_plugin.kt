package build.plugins.support

import build.api.ProjectPlugin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpAsSupportForPlugins
import org.gradle.kotlin.dsl.*

/**
 * @see build.conventions.support._plugin
 */
class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.multipurpose._plugin>()
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpAsSupportForPlugins(this)
})
