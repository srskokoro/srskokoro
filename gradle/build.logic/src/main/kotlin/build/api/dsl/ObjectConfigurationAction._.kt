package build.api.dsl

import org.gradle.api.plugins.ObjectConfigurationAction

@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
inline fun ObjectConfigurationAction.kotlin(module: String) =
	"org.jetbrains.kotlin.$module"
