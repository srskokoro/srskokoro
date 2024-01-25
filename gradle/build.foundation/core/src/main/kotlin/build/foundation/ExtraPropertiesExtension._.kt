package build.foundation

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension

internal fun ExtraPropertiesExtension.parseBoolean(name: String, default: Boolean): Boolean {
	val v = getOrNull<Any>(name) ?: return default
	return if (v is Boolean) v else java.lang.Boolean.parseBoolean(v.toString())
}

internal fun <R> ExtraPropertiesExtension.getOrNull(name: String): R? {
	@Suppress("UNCHECKED_CAST")
	return (if (this is DefaultExtraPropertiesExtension) {
		find(name)
	} else if (has(name)) {
		get(name)
	} else null) as R? // NOTE: This cast throws on non-null incompatible types (as intended).
}
