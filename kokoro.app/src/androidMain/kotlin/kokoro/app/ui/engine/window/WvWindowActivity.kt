package kokoro.app.ui.engine.window

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import kokoro.internal.os.SerializationEncoded

class WvWindowActivity : ComponentActivity() {

	companion object {

		private val COMPONENT_CLASS_NAME: String = WvWindowActivity::class.java.name

		fun shouldHandle(intent: Intent): Boolean {
			val c = intent.component
			return c != null && COMPONENT_CLASS_NAME == c.className
		}

		// --

		private const val EXTRAS_KEY_to_OLD_STATE_ENTRIES = "oldStateEntries"

		private fun <T> WvWindowBusBinding<*, T>.route(
			window: WvWindow, encoded: SerializationEncoded,
		) {
			route(window) { bus -> encoded.decode(bus.serialization) }
		}
	}

	private var handle: WvWindowHandleImpl? = null
	private var window: WvWindow? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		run<Unit> {
			val intent = intent
			val h = WvWindowHandleImpl.get(intent) ?: return@run

			handle = h
			h.attachContext(this@WvWindowActivity)

			val f = WvWindowHandleImpl.getWindowFactory(intent) ?: return@run
			val o = savedInstanceState?.getBundle(EXTRAS_KEY_to_OLD_STATE_ENTRIES) ?: Bundle()
			window = f.init(WvContextImpl(h, oldStateEntries = o))

			return // Success. Skip code below.
		}

		// Either the `Intent` isn't supported or there isn't enough information
		// to process the request.
		//
		// Anyway, treat the "intent" as invalid: finish this activity and
		// prevent it from being restored from the "recents" screen, as it won't
		// make sense otherwise when the activity would never be displayed (as
		// it must "finish" immediately due to an invalid request).
		finishAndRemoveTask()
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent != null) window?.let { w ->
			val busId = WvWindowHandleImpl.getPostBusId(intent) ?: return@let
			val payload = WvWindowHandleImpl.getPostPayload(intent) ?: return@let

			@OptIn(nook::class)
			(w.getDoOnPost_(busId) ?: return@let).route(w, payload)
		}
	}

	override fun onResume() {
		super.onResume()
		window?.onResume()
	}

	override fun onPause() {
		super.onPause()
		window?.onPause()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		@OptIn(nook::class)
		window?.run {
			(context as? WvContextImpl)
		}?.run {
			val o = encodeStateEntries()
			outState.putBundle(EXTRAS_KEY_to_OLD_STATE_ENTRIES, o)
		}
	}

	override fun onDestroy() {
		if (isFinishing) {
			handle?.run {
				detachContext() // So that `finishAndRemoveTask()` isn't called by `close()` below
				close()
			}
			window?.onDestroy()
		}
		super.onDestroy()
	}
}
