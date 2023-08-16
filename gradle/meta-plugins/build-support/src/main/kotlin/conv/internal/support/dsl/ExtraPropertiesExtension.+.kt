package conv.internal.support.dsl

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension

fun ExtraPropertiesExtension.getOrNull(name: String): Any? {
	return if (this is DefaultExtraPropertiesExtension) {
		find(name)
	} else if (has(name)) {
		get(name)
	} else null
}

@Suppress("NOTHING_TO_INLINE")
@JvmName("getOrNull_T")
inline fun <R> ExtraPropertiesExtension.getOrNull(name: String): R? {
	@Suppress("UNCHECKED_CAST")
	return getOrNull(name) as R?
}

inline fun <R> ExtraPropertiesExtension.get(name: String, transform: (Any) -> R, defaultValue: () -> R): R {
	val v = getOrNull(name)
	return if (v != null) {
		transform(v)
	} else {
		defaultValue()
	}
}

@JvmName("get_T")
inline fun <T, R> ExtraPropertiesExtension.get(name: String, transform: (T) -> R, defaultValue: () -> R): R {
	return get(name, {
		@Suppress("UNCHECKED_CAST")
		transform(it as T)
	}, defaultValue)
}

inline fun <R> ExtraPropertiesExtension.getOrElse(name: String, defaultValue: () -> R): R {
	@Suppress("UNCHECKED_CAST")
	return getOrNull(name) as R ?: defaultValue()
}

@Suppress("NOTHING_TO_INLINE")
inline fun <R> ExtraPropertiesExtension.getOrDefault(name: String, defaultValue: R): R {
	@Suppress("UNCHECKED_CAST")
	return getOrNull(name) as R ?: defaultValue
}
