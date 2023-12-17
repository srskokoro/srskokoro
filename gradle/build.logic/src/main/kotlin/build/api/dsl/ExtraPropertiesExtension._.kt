package build.api.dsl

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension

@Suppress("NOTHING_TO_INLINE")
inline fun <R> ExtraPropertiesExtension.getOrThrow(name: String): R {
	@Suppress("UNCHECKED_CAST")
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	return get(name) as R
}

fun <R> ExtraPropertiesExtension.getOrNull(name: String): R? {
	@Suppress("UNCHECKED_CAST")
	return (if (this is DefaultExtraPropertiesExtension) {
		find(name)
	} else if (has(name)) {
		get(name)
	} else null) as R? // NOTE: This cast throws on non-null incompatible types (as intended).
}

inline fun <R> ExtraPropertiesExtension.getOrElse(name: String, defaultValue: ExtraPropertiesExtension.(name: String) -> R): R =
	getOrNull(name) ?: defaultValue(name)

inline fun <R> ExtraPropertiesExtension.getOrAdd(name: String, defaultValue: ExtraPropertiesExtension.(name: String) -> R): R = getOrElse(name) {
	val value = defaultValue(name)
	set(name, value)
	value
}
