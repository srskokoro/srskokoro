package conv.deps

import conv.deps.internal.common.hashCodeOfConcat
import conv.deps.internal.common.removeLast
import conv.deps.serialization.cannotStore
import org.gradle.plugin.use.PluginId as GradlePluginId

sealed class PluginId private constructor() {
	abstract override fun toString(): String
	abstract override fun hashCode(): Int
	abstract override fun equals(other: Any?): Boolean

	@Suppress("MemberVisibilityCanBePrivate")
	companion object {

		fun of(id: Any): PluginId = when (id) {
			is PluginId -> id
			is String -> of(id)
			else -> failOnArgToPluginId(id)
		}

		fun of(id: String): PluginId {
			if (!id.endsWith(".$ANY_NAME")) {
				if (cannotStore(id) || id.indexOf(':') >= 0) {
					failOnPluginId(id)
				}
				return Exact(id)
			}
			return ofAnyName(id.removeLast(ANY_NAME_len_p1))
		}

		internal fun of_unsafe(id: String): PluginId {
			if (!id.endsWith(".$ANY_NAME")) {
				return Exact(id)
			}
			return ofAnyName_unsafe(id.removeLast(ANY_NAME_len_p1))
		}

		fun ofAnyName(namespace: String): PluginId {
			if (cannotStore(namespace) || namespace.indexOf(':') >= 0) {
				failOnPluginId(namespace, ANY_NAME)
			}
			return AnyName(namespace)
		}

		internal fun ofAnyName_unsafe(namespace: String): PluginId {
			return AnyName(namespace)
		}

		// --

		internal fun of(id: GradlePluginId): PluginId = Exact(id.id)

		internal fun ofAnyName(id: GradlePluginId): PluginId = AnyName(id.namespace ?: "")
	}

	// --

	private class Exact(val id: String) : PluginId() {
		override fun toString() = id

		override fun hashCode() = id.hashCode()

		override fun equals(other: Any?) =
			if (this !== other) {
				if (other is Exact) {
					id == other.id
				} else false
			} else true
	}

	private class AnyName(val namespace: String) : PluginId() {
		override fun toString() = "$namespace.$ANY_NAME"

		private val hashCode = hashCodeOfConcat(namespace, ".$ANY_NAME")
		override fun hashCode() = hashCode

		override fun equals(other: Any?) =
			if (this !== other) {
				if (other is AnyName) {
					namespace == other.namespace
				} else false
			} else true
	}
}
