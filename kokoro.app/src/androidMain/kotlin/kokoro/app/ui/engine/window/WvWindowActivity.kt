package kokoro.app.ui.engine.window

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import kokoro.internal.checkNotNull
import kokoro.internal.os.SerializationEncoded

@OptIn(nook::class)
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

	private var handle: WvWindowHandle? = null
	private var window: WvWindow? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		run<Unit> {
			val intent = intent
			val fid = WvWindowHandleBasis.getWindowFactoryIdStr(intent)
				?: return@run // Not a window display request. Ignore.

			val f = checkNotNull(WvWindowFactory.get(fid), or = {
				"No factory registered for window factory ID: $fid"
			})

			val h = WvWindowHandleBasis.get(intent)
				?: return@run // Handle was closed before we can start.

			handle = h
			h.attachContext(this@WvWindowActivity)

			val o = savedInstanceState?.getBundle(EXTRAS_KEY_to_OLD_STATE_ENTRIES) ?: Bundle()
			val wc = WvContextImpl(h, oldStateEntries = o)
			window = f.init(wc) // May throw

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
			val busId = WvWindowHandleBasis.getPostBusId(intent) ?: return@let
			val payload = WvWindowHandleBasis.getPostPayload(intent) ?: return@let
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
