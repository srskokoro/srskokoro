package kokoro.app.ui.engine

import kokoro.internal.DEBUG
import kokoro.internal.JsObj
import kokoro.internal.assert
import kokoro.internal.check
import kokoro.internal.collections.JsMap
import kokoro.internal.collections.iterator
import kokoro.internal.collections.values
import kokoro.internal.unsafeNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic

/**
 * Loads any states saved before, so as to feed it to [UiStates.init]`()`, and
 * sets up a handler for aggregating and encoding new states to save.
 *
 * Must only be called once. Throws otherwise.
 *
 * @see UI_STATES_LOADER
 */
@JsName(UI_STATES_LOADER)
external fun handleUiStates(saver: () -> String): String

object UiStates {

	private val stateEntries = JsMap<String, UiState<*>>()
	private var oldStateEntries: JsObj = unsafeNull()

	private var format: Json = unsafeNull()

	fun init(format: Json, oldStateEntries: JsObj) {
		if (DEBUG) check(UiStates.oldStateEntries.asDynamic() == null, or = { "Must only be called once" })
		UiStates.oldStateEntries = oldStateEntries
		UiStates.format = format
	}

	operator fun <T> get(bus: UiBus<T>): UiState<T> {
		val id = bus.id

		val stateEntries = stateEntries
		stateEntries[id]?.let { entry ->
			assert({ entry.bus == bus }, or = { "Inconsistent bus usage.\n- ID: $id" })
			return entry.unsafeCast<UiState<T>>()
		}

		val oldStateEntries = oldStateEntries
		val enc = oldStateEntries[id]
		val v: T?

		if (enc == null) {
			v = null
		} else {
			val format = format
			val deserializer = bus.serialization(format.serializersModule)

			// See also, https://github.com/cashapp/zipline/blob/1.13.0/zipline/src/jsMain/kotlin/app/cash/zipline/internal/jsonJs.kt
			@OptIn(ExperimentalSerializationApi::class)
			v = format.decodeFromDynamic(deserializer, enc)
			oldStateEntries[id] = null
		}

		val entry = UiState(v, bus)
		stateEntries[id] = entry

		return entry
	}

	fun encode(): JsObj {
		val format = format
		val out = JsObj()
		for (state in stateEntries.values) {
			state.encodeInto(out, format)
		}
		return out
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun <T> UiState<T>.encodeInto(out: JsObj, format: Json) {
		val v = value
		if (v != null) {
			val bus = bus
			val serializer = bus.serialization(format.serializersModule)

			// See also, https://github.com/cashapp/zipline/blob/1.13.0/zipline/src/jsMain/kotlin/app/cash/zipline/internal/jsonJs.kt
			@OptIn(ExperimentalSerializationApi::class)
			val enc = format.encodeToDynamic(serializer, v)
			out[bus.id] = enc
		}
	}
}
