package build.support.dsl

import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.create

inline fun <R> ExtensionContainer.getOrElse(name: String, defaultValue: ExtensionContainer.(name: String) -> R): R {
	@Suppress("UNCHECKED_CAST")
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	return findByName(name) as R? ?: defaultValue(name)
}

inline fun <reified R : Any> ExtensionContainer.getOrAdd(name: String, defaultValue: ExtensionContainer.(name: String) -> R): R = getOrElse(name) {
	val extension = defaultValue(name)
	add<R>(name, extension)
	extension
}

inline fun <reified R : Any> ExtensionContainer.getOrCreate(name: String): R = getOrElse(name) { create(name) }
