package kokoro.app.ui.engine.window

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.UiState
import kokoro.internal.ASSERTIONS_ENABLED
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.errorAssertion
import kokoro.internal.os.SerializationEncoded
import kokoro.internal.os.SerializationEncoded.Companion.getSerializationEncoded
import kokoro.internal.os.SerializationEncoded.Companion.putSerializationEncoded
import kotlinx.serialization.modules.SerializersModule

@nook internal class WvContextImpl(
	handle: WvWindowHandle,
	private val activity: WvWindowActivity,
	/** NOTE: Can be [Bundle.EMPTY] (which is unmodifiable). */
	private val oldStateEntries: Bundle,
) : WvContext(handle, activity.lifecycleScope) {

	override var title: CharSequence?
		@MainThread get() = activity.title
		@MainThread set(v) {
			assertThreadMain()
			activity.title = v
		}

	@MainThread
	override fun load(url: String) {
		assertThreadMain()
		activity.loadUrl(url)
	}

	@MainThread
	override fun finish() {
		assertThreadMain()
		activity.finish()
	}

	// --

	@MainThread
	override fun <T> loadOldState(bus: UiBus<T>): T? {
		assertThreadMain()

		val src = oldStateEntries
		val id = bus.id
		src.getSerializationEncoded(id)?.let { enc ->
			val v = enc.decode(bus.serialization) // May throw
			src.remove(id) // Does nothing on `Bundle.EMPTY`
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
				out.putSerializationEncoded(bus.id, enc)
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
