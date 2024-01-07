package build.foundation

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension

internal fun ExtraPropertiesExtension.parseBoolean(name: String, default: Boolean): Boolean {
	return java.lang.Boolean.parseBoolean((getOrNull<Any>(name) ?: return default).toString())
}

internal fun <R> ExtraPropertiesExtension.getOrNull(name: String): R? {
	@Suppress("UNCHECKED_CAST")
	return (if (this is DefaultExtraPropertiesExtension) {
		find(name)
	} else if (has(name)) {
		get(name)
	} else null) as R? // NOTE: This cast throws on non-null incompatible types (as intended).
}
