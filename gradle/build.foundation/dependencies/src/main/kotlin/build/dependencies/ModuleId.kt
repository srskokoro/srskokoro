package build.dependencies

import build.support.from
import build.support.hashCodeWith
import build.support.until
import org.gradle.api.IllegalDependencyNotation

sealed class ModuleId private constructor() {
	abstract val group: String
	abstract val name: String

	abstract override fun toString(): String

	abstract override fun hashCode(): Int
	abstract override fun equals(other: Any?): Boolean

	companion object {

		fun of(id: String): ModuleId {
			val groupEnd = id.indexOf(':')
			val nameEnd = id.indexOf(':', groupEnd + 1)
			if (groupEnd or nameEnd.inv() < 0) {
				throw E_InvalidModuleId(id)
			}
			return ViaId(id, groupEnd)
		}

		internal fun of_unsafe(id: String): ModuleId {
			val groupEnd = id.indexOf(':')
			return ViaId(id, groupEnd)
		}

		fun of(group: String, name: String): ModuleId {
			// NOTE: The bitwise-AND of two negatives (i.e., two set sign bits)
			// is a negative. On the other hand, if at least one is positive,
			// the result of the bitwise-AND would be positive.
			if (group.indexOf(':') and name.indexOf(':') >= 0) {
				throw E_InvalidModuleId(group, name)
			}
			return ViaGroupName(group, name)
		}

		internal fun of_unsafe(group: String, name: String): ModuleId {
			return ViaGroupName(group, name)
		}
	}

	private class ViaId(
		val id: String,
		val groupEnd: Int,
	) : ModuleId() {
		override fun toString() = id

		override fun hashCode() = id.hashCode()

		override fun equals(other: Any?) =
			if (this !== other) when (other) {
				is ViaId -> id == other.id
				is ViaGroupName -> equals0(other)
				else -> false
			} else true

		internal fun equals0(other: ViaGroupName) =
			groupEnd == other.group.length &&
				id.startsWith(other.group) &&
				id.endsWith(other.name)

		override val group: String get() = id.until(groupEnd)
		override val name: String get() = id.from(groupEnd + 1)
	}

	private class ViaGroupName(
		override val group: String,
		override val name: String,
	) : ModuleId() {
		override fun toString() = "$group:$name"

		private val hashCode = group.hashCodeWith(':').hashCodeWith(name)
		override fun hashCode() = hashCode

		override fun equals(other: Any?) =
			if (this !== other) when (other) {
				is ViaGroupName -> group == other.group && name == other.name
				is ViaId -> other.equals0(this)
				else -> false
			} else true
	}
}

private fun E_InvalidModuleId(group: String, name: String) = E_InvalidModuleId("$group:$name")

private fun E_InvalidModuleId(moduleId: String) = IllegalDependencyNotation(
	"""
	Supplied module identifier "$moduleId" is invalid.
	Example identifiers: "org.gradle:gradle-core", "org.mockito:mockito-core"
	""".trimIndent()
)
