package build.dependencies

import org.gradle.api.IllegalDependencyNotation

sealed class PluginId private constructor() {
	abstract override fun toString(): String
	abstract override fun hashCode(): Int
	abstract override fun equals(other: Any?): Boolean

	companion object {

		fun of(id: String): PluginId {
			if (id.indexOf(':') >= 0) {
				throw E_InvalidPluginId(id)
			}
			return Exact(id)
		}

		internal fun of_unsafe(id: String): PluginId {
			return Exact(id)
		}
	}

	private data class Exact(val id: String) : PluginId() {
		override fun toString() = id
	}
}

private fun E_InvalidPluginId(pluginId: String) = IllegalDependencyNotation(
	"""
	Supplied plugin identifier "$pluginId" is invalid.
	Example identifiers: "org.jetbrains.kotlin.jvm", "com.android.application"
	""".trimIndent()
)
