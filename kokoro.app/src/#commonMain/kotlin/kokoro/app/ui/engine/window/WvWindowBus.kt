package kokoro.app.ui.engine.window

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.jvm.JvmField
import kotlin.reflect.typeOf

inline fun <reified T> WvWindowBus(
	tag: String? = null,
	noinline serialization: SerializersModule.() -> KSerializer<T> = WvWindowBus.defaultSerialization<T>(),
) = WvWindowBus(
	serialization,
	typeOf<T>().toString().let { type ->
		if (tag == null) type else "$type#$tag"
	},
)

data class WvWindowBus<T>(
	@JvmField val serialization: SerializersModule.() -> KSerializer<T>,
	@JvmField val id: String,
) {
	companion object {
		inline fun <reified T> defaultSerialization(): SerializersModule.() -> KSerializer<T> = { serializer() }
	}
}
