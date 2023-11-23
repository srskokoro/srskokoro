package build.deps

import org.gradle.jvm.toolchain.JvmImplementation
import kotlin.reflect.KProperty0
import org.gradle.jvm.toolchain.JvmImplementation as V

class JvmSetupImplementation private constructor(val value: V, internal val id: String) {
	override fun toString() = id

	companion object {
		/**
		 * WARNING: Used by [registerKnown]`()`. Do not move this field below
		 * any usage of that method.
		 */
		private val knownMap: MutableMap<String, JvmSetupImplementation> = HashMap()

		//@formatter:off
		/** @see JvmImplementation.VENDOR_SPECIFIC */ val VENDOR_SPECIFIC = registerKnown(V::VENDOR_SPECIFIC)
		/** @see JvmImplementation.J9 */ val J9 = registerKnown(V::J9)
		//@formatter:on

		fun parseOrNull(s: String) = knownMap[s]

		@Suppress("NOTHING_TO_INLINE")
		inline fun parse(s: String) = parseOrNull(s) ?: failOnJvmSetupImplementation(s)

		private fun registerKnown(src: KProperty0<V>): JvmSetupImplementation {
			val name = src.name
			val it = JvmSetupImplementation(src.get(), name)
			if (knownMap.putIfAbsent(name, it) != null) {
				throw AssertionError("Unexpected duplicate: $name")
			}
			return it
		}
	}
}
