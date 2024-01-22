package kokoro.internal.scoping

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class ScopingCompilerPluginRegistrar : CompilerPluginRegistrar() {

	override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
		FirExtensionRegistrarAdapter.registerExtension(ScopingExtensionRegistrar())
	}

	override val supportsK2: Boolean
		get() = true
}
