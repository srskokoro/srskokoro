package kokoro.internal.scoping

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class ScopingExtensionRegistrar : FirExtensionRegistrar() {
	override fun ExtensionRegistrarContext.configurePlugin() {
		+::ScopingCheckersExtension
	}
}
