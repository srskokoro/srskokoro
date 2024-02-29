package kokoro.app.ui.engine

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.jvm.JvmField
import kotlin.reflect.typeOf

@Suppress("DataClassPrivateConstructor")
data class UiBus<T> private constructor(
	@JvmField val id: String,
	@JvmField val serialization: SerializersModule.() -> KSerializer<T>,
) {
	companion object {
		inline fun <reified T> defaultSerialization(): SerializersModule.() -> KSerializer<T> = { serializer() }

		/**
		 * @see UiBus.of
		 */
		inline fun <reified T> id(tag: String? = null): String {
			return typeOf<T>().toString().let { type ->
				if (tag == null) type else "$type#$tag"
			}
		}

		inline fun <reified T> of(
			tag: String? = null,
			noinline serialization: SerializersModule.() -> KSerializer<T> = defaultSerialization<T>(),
		) = wrap(id<T>(tag), serialization)

		/**
		 * @see UiBus.of
		 * @see UiBus.id
		 */
		fun <T> wrap(
			id: String,
			serialization: SerializersModule.() -> KSerializer<T>,
		) = UiBus(id, serialization)

		/**
		 * @see UiBus.of
		 * @see UiBus.id
		 * @see UiBus.wrap
		 */
		inline fun <reified T> wrap(id: String) = wrap(id, defaultSerialization<T>())
	}
}
