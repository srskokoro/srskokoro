package build.kt.jvm.multipurpose

import build.api.ProjectPlugin
import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import build.conventions.internal.ensureMultipurpose
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.lib._plugin>()
	}

	@OptIn(InternalConventionsApi::class)
	InternalConventions.ensureMultipurpose(this)
})
