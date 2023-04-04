package conv.deps

import conv.deps.internal.common.remove
import org.gradle.jvm.toolchain.JvmVendorSpec
import kotlin.reflect.KProperty0
import org.gradle.jvm.toolchain.JvmVendorSpec as V

class JvmSetupVendor private constructor(val value: V, internal val id: String) {
	override fun toString() = id

	@Suppress("UnstableApiUsage", "SpellCheckingInspection")
	companion object {
		private const val PREFIX_MATCHING = "matching:"
		private const val PREFIX_MATCHING_len = PREFIX_MATCHING.length

		/**
		 * WARNING: Used by [registerKnown]`()`. Do not move this field below
		 * any usage of that method.
		 */
		private val knownMap: MutableMap<String, JvmSetupVendor> = HashMap()

		//@formatter:off
		/** @see JvmVendorSpec.ADOPTIUM */ val ADOPTIUM = registerKnown(V::ADOPTIUM)
		/** @see JvmVendorSpec.ADOPTOPENJDK */ val ADOPTOPENJDK = registerKnown(V::ADOPTOPENJDK)
		/** @see JvmVendorSpec.AMAZON */ val AMAZON = registerKnown(V::AMAZON)
		/** @see JvmVendorSpec.APPLE */ val APPLE = registerKnown(V::APPLE)
		/** @see JvmVendorSpec.AZUL */ val AZUL = registerKnown(V::AZUL)
		/** @see JvmVendorSpec.BELLSOFT */ val BELLSOFT = registerKnown(V::BELLSOFT)
		/** @see JvmVendorSpec.GRAAL_VM */ val GRAAL_VM = registerKnown(V::GRAAL_VM)
		/** @see JvmVendorSpec.HEWLETT_PACKARD */ val HEWLETT_PACKARD = registerKnown(V::HEWLETT_PACKARD)
		/** @see JvmVendorSpec.IBM */ val IBM = registerKnown(V::IBM)
		/** @see JvmVendorSpec.IBM_SEMERU */ val IBM_SEMERU = registerKnown(V::IBM_SEMERU)
		/** @see JvmVendorSpec.MICROSOFT */ val MICROSOFT = registerKnown(V::MICROSOFT)
		/** @see JvmVendorSpec.ORACLE */ val ORACLE = registerKnown(V::ORACLE)
		/** @see JvmVendorSpec.SAP */ val SAP = registerKnown(V::SAP)
		//@formatter:on

		fun matching(match: String) = JvmSetupVendor(V.matching(match), "$PREFIX_MATCHING$match")

		fun parseOrNull(s: String) = if (!s.startsWith(PREFIX_MATCHING)) knownMap[s] else {
			val match = s.remove(PREFIX_MATCHING_len)
			JvmSetupVendor(V.matching(match), s)
		}

		@Suppress("NOTHING_TO_INLINE")
		inline fun parse(s: String) = parseOrNull(s) ?: failOnJvmSetupVendor(s)

		private fun registerKnown(src: KProperty0<V>): JvmSetupVendor {
			val name = src.name
			val it = JvmSetupVendor(src.get(), name)
			if (knownMap.putIfAbsent(name, it) != null) {
				throw AssertionError("Unexpected duplicate: $name")
			}
			return it
		}
	}
}
