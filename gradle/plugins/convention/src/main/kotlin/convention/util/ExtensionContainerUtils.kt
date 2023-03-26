package convention.util

import org.gradle.api.Named
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.add

inline infix fun <reified T : Named> T.asExtensionIn(extendable: ExtensionAware) = asExtensionIn(extendable.extensions)

inline infix fun <reified T : Named> T.asExtensionIn(extensions: ExtensionContainer) {
	extensions.add<T>(name, this)
}
