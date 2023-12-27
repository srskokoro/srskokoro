package build.api.dsl

import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.*

@Suppress("NOTHING_TO_INLINE")
inline fun <R> ExtensionContainer.getOrThrow(name: String): R {
	@Suppress("UNCHECKED_CAST")
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	return getByName(name) as R
}

@Suppress("NOTHING_TO_INLINE")
inline fun <R> ExtensionContainer.getOrNull(name: String): R? {
	@Suppress("UNCHECKED_CAST")
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	return findByName(name) as R?
}

@Suppress("NOTHING_TO_INLINE")
inline fun <R> ExtensionContainer.getSafelyOrNull(name: String): R? {
	@Suppress("UNCHECKED_CAST")
	return findByName(name) as? R?
}


inline fun <R> ExtensionContainer.getOrElse(name: String, defaultValue: ExtensionContainer.(name: String) -> R): R {
	return getOrNull(name) ?: defaultValue(name)
}

inline fun <R> ExtensionContainer.getSafelyOrElse(name: String, defaultValue: ExtensionContainer.(name: String) -> R): R {
	return getSafelyOrNull(name) ?: defaultValue(name)
}


inline fun <reified R : Any> ExtensionContainer.getOrAdd(name: String, defaultValue: ExtensionContainer.(name: String) -> R): R = getOrElse(name) {
	val extension = defaultValue(name)
	add<R>(name, extension)
	extension
}

inline fun <reified R : Any> ExtensionContainer.getOrCreate(name: String): R = getOrElse(name) { create(name) }
