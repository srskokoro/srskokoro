package build.deps

class Version private constructor(val value: String) {
	override fun toString() = value
	override fun hashCode() = value.hashCode()
	override fun equals(other: Any?) =
		if (this !== other) {
			if (other is Version) {
				value == other.value
			} else false
		} else true

	@Suppress("MemberVisibilityCanBePrivate")
	companion object {

		fun of(version: Any): Version = when (version) {
			is Version -> version
			is String -> of(version)
			else -> failOnArgToVersion(version)
		}

		fun of(version: String): Version {
			if (version.indexOf(':') >= 0) {
				failOnVersion(version)
			}
			return Version(version)
		}

		internal fun of_unsafe(version: String): Version {
			return Version(version)
		}
	}
}
