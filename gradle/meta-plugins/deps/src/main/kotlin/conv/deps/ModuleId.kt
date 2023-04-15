package conv.deps

import conv.deps.internal.common.*
import conv.deps.serialization.cannotStore
import org.gradle.api.artifacts.ModuleVersionSelector

sealed class ModuleId private constructor() {
	abstract val group: String
	abstract val name: String

	abstract override fun toString(): String
	abstract fun toString(version: String): String

	abstract override fun hashCode(): Int
	abstract override fun equals(other: Any?): Boolean

	@Suppress("MemberVisibilityCanBePrivate")
	companion object {

		fun of(id: Any): ModuleId = when (id) {
			is ModuleId -> id
			is String -> of(id)
			else -> failOnArgToModuleId(id)
		}

		fun of(id: String): ModuleId {
			if (!id.endsWith(":$ANY_NAME")) {
				if (cannotStore(id)) {
					failOnModuleId(id)
				}
				val groupEnd = id.indexOf(':')
				val nameEnd = id.indexOf(':', groupEnd + 1)
				if (groupEnd or nameEnd.inv() < 0) {
					failOnModuleId(id)
				}
				return ViaId(id, groupEnd)
			}
			return ofAnyName(id.removeLast(ANY_NAME_len_p1))
		}

		internal fun of_unsafe(id: String): ModuleId {
			if (!id.endsWith(":$ANY_NAME")) {
				val groupEnd = id.indexOf(':')
				return ViaId(id, groupEnd)
			}
			return ofAnyName_unsafe(id.removeLast(ANY_NAME_len_p1))
		}

		fun of(group: String, name: String): ModuleId {
			if (name != ANY_NAME) {
				if (cannotStore(group) || group.indexOf(':') and name.indexOf(':') >= 0) {
					failOnModuleId(group, name)
				}
				return ViaGroupName(group, name)
			}
			return ofAnyName(group)
		}

		internal fun of_unsafe(group: String, name: String): ModuleId {
			if (name != ANY_NAME) {
				return ViaGroupName(group, name)
			}
			return ofAnyName_unsafe(group)
		}

		fun ofAnyName(group: String): ModuleId {
			if (cannotStore(group) || group.indexOf(':') >= 0) {
				failOnModuleId(group, ANY_NAME)
			}
			return ViaGroupName(group, ANY_NAME)
		}

		internal fun ofAnyName_unsafe(group: String): ModuleId {
			return ViaGroupName(group, ANY_NAME)
		}

		// --

		internal fun of(dependency: ModuleVersionSelector): ModuleId = ViaGroupName(dependency.group, dependency.name)

		internal fun ofAnyName(dependency: ModuleVersionSelector): ModuleId = ViaGroupName(dependency.group, ANY_NAME)
	}

	@Suppress("NOTHING_TO_INLINE")
	@JvmName("toString?")
	inline fun toString(version: String?) =
		if (version == null) toString() else toString(version)

	@Suppress("NOTHING_TO_INLINE")
	inline fun toString(version: Version?) =
		if (version == null) toString() else toString(version.value)

	// --

	private class ViaId(
		val id: String,
		val groupEnd: Int,
	) : ModuleId() {
		override fun toString() = id
		override fun toString(version: String) = "$id:$version"

		override fun hashCode() = id.hashCode()

		override fun equals(other: Any?) =
			if (this !== other) when (other) {
				is ViaId -> id == other.id
				is ViaGroupName -> equals2(other)
				else -> false
			} else true

		internal fun equals2(other: ViaGroupName) =
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
		override fun toString(version: String) = "$group:$name:$version"

		private val hashCode = hashCodeOfConcat(group, name)
		override fun hashCode() = hashCode

		override fun equals(other: Any?) =
			if (this !== other) when (other) {
				is ViaGroupName -> group == other.group && name == other.name
				is ViaId -> other.equals2(this)
				else -> false
			} else true
	}
}
