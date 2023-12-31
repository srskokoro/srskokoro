package build.plugins.support

import build.api.ProjectPlugin
import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import build.conventions.internal.setUpAsSupportForPlugins
import org.gradle.kotlin.dsl.*

/**
 * @see build.conventions.support._plugin
 */
class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.multipurpose._plugin>()
	}

	@OptIn(InternalConventionsApi::class)
	InternalConventions.setUpAsSupportForPlugins(this)
})
