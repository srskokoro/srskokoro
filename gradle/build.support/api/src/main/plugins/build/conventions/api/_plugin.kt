package build.conventions.api

import build.api.UtilityPlugin
import build.conventions.internal.InternalConventionsApi
import build.conventions.internal.throwOnNonConventionsRoot
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

@OptIn(InternalConventionsApi::class)
class _plugin : UtilityPlugin({
	when (this) {
		is Project -> throwOnNonConventionsRoot()
		is Settings -> throwOnNonConventionsRoot()
		else -> throw UnsupportedOperationException()
	}
})
