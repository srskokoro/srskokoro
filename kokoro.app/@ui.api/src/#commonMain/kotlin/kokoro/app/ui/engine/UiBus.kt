package kokoro.app.ui.engine

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.jvm.JvmField
import kotlin.reflect.typeOf

inline fun <reified T> UiBus(
	noinline serialization: SerializersModule.() -> KSerializer<T> = UiBus.defaultSerialization<T>(),
) = UiBus(
	serialization,
	UiBus.id<T>(),
)

inline fun <reified T> UiBus(
	tag: String?,
	noinline serialization: SerializersModule.() -> KSerializer<T> = UiBus.defaultSerialization<T>(),
) = UiBus(
	serialization,
	UiBus.id<T>(tag),
)

data class UiBus<T>(
	@JvmField val serialization: SerializersModule.() -> KSerializer<T>,
	@JvmField val id: String,
) {
	companion object {
		inline fun <reified T> defaultSerialization(): SerializersModule.() -> KSerializer<T> = { serializer() }

		inline fun <reified T> id(tag: String? = null): String {
			return typeOf<T>().toString().let { type ->
				if (tag == null) type else "$type#$tag"
			}
		}
	}
}
