package kokoro.internal.os

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.check
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@JvmInline
value class SerializationEncoded private constructor(private val data: ByteArray) {

	fun putInto(bundle: Bundle, key: String?) {
		bundle.putByteArray(key, data)
	}

	fun putInto(intent: Intent, key: String?) {
		intent.putExtra(key, data)
	}

	fun writeInto(parcel: Parcel) {
		parcel.writeByteArray(data)
	}

	// --

	inline fun <reified T> decode(): T = decode { serializer() }

	inline fun <T> decode(deserialization: SerializersModule.() -> DeserializationStrategy<T>) = decode(deserialization.invoke(module))

	fun <T> decode(deserializer: DeserializationStrategy<T>): T {
		@OptIn(ExperimentalSerializationApi::class)
		return cbor.decodeFromByteArray(deserializer, data)
	}

	companion object {

		inline operator fun <reified T> invoke(value: T) = invoke(value) { serializer() }

		inline operator fun <T> invoke(value: T, serialization: SerializersModule.() -> SerializationStrategy<T>) = invoke(value, serialization.invoke(module))

		operator fun <T> invoke(value: T, serializer: SerializationStrategy<T>): SerializationEncoded {
			@OptIn(ExperimentalSerializationApi::class)
			return SerializationEncoded(cbor.encodeToByteArray(serializer, value))
		}

		// --

		fun getFrom(bundle: Bundle, key: String?) =
			bundle.getByteArray(key)?.let { SerializationEncoded(it) }

		fun getFrom(intent: Intent, key: String?) =
			intent.getByteArrayExtra(key)?.let { SerializationEncoded(it) }

		fun readFrom(parcel: Parcel) =
			parcel.createByteArray()?.let { SerializationEncoded(it) }

		// --

		@OptIn(ExperimentalSerializationApi::class)
		private inline val cbor: Cbor get() = @Suppress("DEPRECATION_ERROR") ModuleHolder.cbor

		inline val module: SerializersModule get() = @Suppress("DEPRECATION_ERROR") ModuleHolder.value

		fun init(module: SerializersModule) {
			with(ModuleHolderInit) {
				if (DEBUG) check(value == null, or = { "Must only be called once" })
				value = module
			}
		}
	}

	private object ModuleHolderInit {
		@JvmField var value: SerializersModule? = null
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object ModuleHolder {

		@JvmField val value = ModuleHolderInit.value ?: error(
			"Must first call `${Companion::class.qualifiedName}.${::init.name}()`"
		)

		@OptIn(ExperimentalSerializationApi::class)
		@JvmField val cbor = Cbor { serializersModule = value }
	}
}
