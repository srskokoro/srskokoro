package kokoro.app.ui.engine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.jvm.JvmField
import kotlin.reflect.typeOf

@Suppress("DataClassPrivateConstructor")
data class UiBus<T> private constructor(
	@JvmField val id: String,
	@JvmField val serialization: SerializersModule.() -> KSerializer<T>,
) {
	private object NothingHolder {
		@JvmField val NULLABLE_NOTHING_SERIALIZER: KSerializer<Nothing?> = @OptIn(ExperimentalSerializationApi::class) NothingSerializer().nullable
		@JvmField val NOTHING: UiBus<Nothing?> = wrap(::NOTHING.name) { NULLABLE_NOTHING_SERIALIZER }
	}

	companion object {

		val NOTHING get() = NothingHolder.NOTHING

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
		 */
		// Exists simply because of IDE auto-complete annoyance in which the
		// first result is often the one with the lambda block.
		inline fun <reified T> of() = of<T>(null)

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
