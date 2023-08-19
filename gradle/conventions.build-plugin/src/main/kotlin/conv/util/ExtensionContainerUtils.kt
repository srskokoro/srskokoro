package conv.util

import org.gradle.api.Named
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.add

inline infix fun <reified T : Named> T.asExtensionIn(extendable: ExtensionAware) = asExtensionIn(extendable.extensions)

inline infix fun <reified T : Named> T.asExtensionIn(extensions: ExtensionContainer): T {
	extensions.add<T>(name, this)
	return this
}

inline fun <reified T : Named> T.asExtensionIn(extendable: ExtensionAware, crossinline preconfigure: T.() -> Unit) {
	preconfigure()
	asExtensionIn(extendable = extendable)
}

inline fun <reified T : Named> T.asExtensionIn(extensions: ExtensionContainer, crossinline preconfigure: T.() -> Unit) {
	preconfigure()
	asExtensionIn(extensions = extensions)
}
