package build.dependencies

import org.gradle.api.UnknownDomainObjectException

class DepsProps(
	val map: Map<String, String>,
) {
	/**
	 * @see DepsProps.map
	 * @see Deps.prop
	 */
	@Suppress("NOTHING_TO_INLINE")
	inline operator fun get(key: String) = map[key] ?: throw E_UnknownPropKey(key)
}

@PublishedApi
internal fun E_UnknownPropKey(key: String) = UnknownDomainObjectException(
	"Could not find property with key: $key"
)
