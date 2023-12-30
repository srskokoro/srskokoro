package build.conventions.internal

import org.gradle.api.plugins.ObjectConfigurationAction

@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
internal inline fun ObjectConfigurationAction.kotlin(module: String) =
	"org.jetbrains.kotlin.$module"
