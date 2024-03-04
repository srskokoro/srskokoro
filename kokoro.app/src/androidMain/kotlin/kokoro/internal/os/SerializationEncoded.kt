package kokoro.internal.os

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.check
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

class SerializationEncoded private constructor(private val data: ByteArray) : Parcelable {

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
			return wrap(cbor.encodeToByteArray(serializer, value))
		}

		@Suppress("NOTHING_TO_INLINE")
		private inline fun wrap(data: ByteArray) = SerializationEncoded(data)

		// --

		/** @see Bundle.getSerializationEncoded */
		@Suppress("NOTHING_TO_INLINE")
		inline fun Bundle.putSerializationEncoded(key: String?, encoded: SerializationEncoded?) = putParcelable(key, encoded)

		/** @see Bundle.putSerializationEncoded */
		fun Bundle.getSerializationEncoded(key: String?): SerializationEncoded? {
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				getParcelable(key, SerializationEncoded::class.java)
			} else {
				@Suppress("DEPRECATION")
				getParcelable(key)
			}
		}

		/** @see Intent.putExtra */
		fun Intent.getSerializationEncodedExtra(name: String?): SerializationEncoded? {
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				getParcelableExtra(name, SerializationEncoded::class.java)
			} else {
				@Suppress("DEPRECATION")
				getParcelableExtra(name)
			}
		}

		/** @see Parcel.readSerializationEncoded */
		fun Parcel.writeSerializationEncoded(encoded: SerializationEncoded): Unit = encoded.writeToParcel(this)

		/** @see Parcel.writeSerializationEncoded */
		fun Parcel.readSerializationEncoded(): SerializationEncoded = readFromParcel(this)

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

		// --

		@Suppress("unused")
		@JvmField val CREATOR = object : Parcelable.Creator<SerializationEncoded> {

			override fun newArray(size: Int) = arrayOfNulls<SerializationEncoded?>(size)

			override fun createFromParcel(parcel: Parcel) = readFromParcel(parcel)
		}

		@Suppress("NOTHING_TO_INLINE")
		private inline fun readFromParcel(parcel: Parcel) = wrap(parcel.createByteArray()!!)
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun writeToParcel(parcel: Parcel): Unit = parcel.writeByteArray(data)

	override fun writeToParcel(parcel: Parcel, flags: Int): Unit = writeToParcel(parcel)

	override fun describeContents(): Int = 0

	// --

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
