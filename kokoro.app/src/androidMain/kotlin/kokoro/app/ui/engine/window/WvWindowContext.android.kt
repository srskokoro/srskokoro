package kokoro.app.ui.engine.window

import android.os.Bundle
import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.UiState
import kokoro.internal.ASSERTIONS_ENABLED
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.errorAssertion
import kokoro.internal.os.SerializationEncoded
import kotlinx.serialization.modules.SerializersModule

@nook internal class WvContextImpl(
	handle: WvWindowHandle,
	private val oldStateEntries: Bundle,
) : WvContext(handle) {

	@MainThread
	override fun load(url: String) {
		assertThreadMain()
		TODO("Not yet implemented")
	}

	@MainThread
	override fun finish() {
		assertThreadMain()
		handle.activity?.finish()
	}

	// --

	@MainThread
	override fun <T> loadOldState(bus: UiBus<T>): T? {
		assertThreadMain()

		val src = oldStateEntries
		val id = bus.id
		SerializationEncoded.getFrom(src, id)?.let { enc ->
			val v = enc.decode(bus.serialization) // May throw
			src.remove(id)
			return v
		}
		return null
	}

	@MainThread
	@nook fun encodeStateEntries(): Bundle {
		assertThreadMain()

		val out = Bundle()
		out.putAll(oldStateEntries)

		val m = SerializationEncoded.module
		stateEntries.forEachValue { it.encodeInto(out, m) }

		return out
	}

	companion object {
		private fun <T> UiState<T>.encodeInto(out: Bundle, module: SerializersModule) {
			val v = value
			if (v != null) {
				val bus = bus
				val serializer = bus.serialization.invoke(module)
				val enc = SerializationEncoded(v, serializer) // May throw
				enc.putInto(out, bus.id)
			} else {
				// NOTE: The old state entry data should've been discarded
				// already, since we now have a `UiState` instance loaded.
				if (ASSERTIONS_ENABLED) bus.id.let { id ->
					if (out.containsKey(id)) errorAssertion(
						"Unexpected: old state entry data still exists at ID $id"
					)
				}
			}
		}
	}
}
