package build.foundation

import org.gradle.api.plugins.ObjectConfigurationAction

@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
@InternalApi
inline fun ObjectConfigurationAction.kotlin(module: String) =
	"org.jetbrains.kotlin.$module"
