package build.conventions.api

import build.api.UtilityPlugin
import build.conventions.throwOnNonConventionsRoot
import build.foundation.InternalApi
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

@OptIn(InternalApi::class)
class _plugin : UtilityPlugin({
	when (this) {
		is Project -> throwOnNonConventionsRoot()
		is Settings -> throwOnNonConventionsRoot()
		else -> throw UnsupportedOperationException()
	}
})
