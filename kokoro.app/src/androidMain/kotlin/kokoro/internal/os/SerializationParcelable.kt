package kokoro.internal.os

import android.os.Parcel
import android.os.Parcelable
import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.assert
import kokoro.internal.check
import kokoro.internal.require
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

class SerializationParcelable : Parcelable {

	private var value: Any? = null
	private var serializer: KSerializer<*>? = null

	constructor()

	inline fun <reified T> set(value: T) = set(value, module.serializer())

	fun <T> set(value: T, serializer: KSerializer<T>) {
		this.value = value
		this.serializer = serializer
	}

	inline fun <reified T> get(): T = get(module.serializer())

	fun <T> get(serializer: KSerializer<T>): T {
		if (this.serializer != null) {
			if (DEBUG) require(this.serializer == serializer, or_fail_with = {
				"`serializer` used must be the same as before"
			})
			@Suppress("UNCHECKED_CAST")
			return value as T
		}

		val encoded = value
		if (encoded is ByteArray) {
			// TODO Use a custom sequential decoder instead that decodes directly from a byte array.
			//  - For inspiration, see, https://github.com/chRyNaN/serialization-parcelable/blob/0.8.0/parcelable-core/src/commonMain/kotlin/com/chrynan/parcelable/core/ParcelDecoder.kt
			//  - See also, https://github.com/Kotlin/kotlinx.serialization/blob/v1.6.2/docs/formats.md#custom-formats-experimental
			@OptIn(ExperimentalSerializationApi::class)
			val decoded = cbor.decodeFromByteArray(serializer, encoded)

			this.serializer = serializer
			value = decoded

			return decoded
		}

		assert({ encoded == null }) {
			"Unexpected encoded type: ${encoded!!::class.qualifiedName}\n- Value: $encoded"
		}
		@Suppress("UNCHECKED_CAST")
		return null as T
	}

	// --

	override fun describeContents() = 0

	constructor(parcel: Parcel) {
		value = parcel.createByteArray()
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		val serializer = this.serializer
		if (serializer != null) {
			// TODO Use a custom sequential encoder instead that encodes directly to a byte array.
			//  - For inspiration, see, https://github.com/chRyNaN/serialization-parcelable/blob/0.8.0/parcelable-core/src/commonMain/kotlin/com/chrynan/parcelable/core/ParcelEncoder.kt
			//  - See also, https://github.com/Kotlin/kotlinx.serialization/blob/v1.6.2/docs/formats.md#custom-formats-experimental
			@Suppress("UNCHECKED_CAST")
			@OptIn(ExperimentalSerializationApi::class)
			cbor.encodeToByteArray(serializer as KSerializer<Any?>, value)
		} else {
			val encoded = value
			if (encoded is ByteArray) {
				encoded
			} else {
				assert({ encoded == null }) {
					"Unexpected encoded type: ${encoded!!::class.qualifiedName}\n- Value: $encoded"
				}
				ByteArray(0)
			}
		}.let {
			dest.writeByteArray(it)
		}
	}

	companion object CREATOR : Parcelable.Creator<SerializationParcelable> {

		@OptIn(ExperimentalSerializationApi::class)
		private inline val cbor: Cbor
			get() = @Suppress("DEPRECATION_ERROR") ModuleHolder.cbor

		inline val module: SerializersModule
			get() = @Suppress("DEPRECATION_ERROR") ModuleHolder.value

		fun init(module: SerializersModule) {
			with(ModuleHolderInit) {
				if (DEBUG) check(value == null, or_fail_with = {
					"Must only be called once"
				})
				value = module
			}
		}

		override fun createFromParcel(parcel: Parcel) = SerializationParcelable(parcel)

		override fun newArray(size: Int) = arrayOfNulls<SerializationParcelable?>(size)
	}

	private object ModuleHolderInit {
		@JvmField var value: SerializersModule? = null
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object ModuleHolder {

		@JvmField val value = ModuleHolderInit.value ?: error(
			"Must first call `${CREATOR::class.qualifiedName}.${::init.name}()`"
		)

		@OptIn(ExperimentalSerializationApi::class)
		@JvmField val cbor = Cbor { serializersModule = value }
	}
}
