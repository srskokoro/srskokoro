package kokoro.app.ui.engine.window

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.webkit.WebViewClientCompat
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.checkNotNull
import kokoro.internal.os.SerializationEncoded
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(nook::class)
open class WvWindowActivity : ComponentActivity() {

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

	@MainThread
	open fun initHandle(): WvWindowHandle? {
		// Returns `null` if `intent` isn't a window display request or the
		// handle was closed before we can start.
		return WvWindowHandle.get(intent)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		run<Unit> {
			val h = initHandle() ?: return@run

			val fid = h.windowFactoryId
			val f = checkNotNull(WvWindowFactory.get(fid), or = {
				"No factory registered for window factory ID: $fid"
			})

			handle = h
			h.attachPeer(this)

			val o = savedInstanceState?.getBundle(EXTRAS_KEY_to_OLD_STATE_ENTRIES) ?: Bundle.EMPTY
			val wc = WvContextImpl(h, this, oldStateEntries = o)
			window = f.init(wc) // May throw

			wc.scope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
				setUpWebView()
			}
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

	private var wv: WebView? = null
	private var initUrl: String? = null

	@MainThread
	fun loadUrl(url: String) {
		assertThreadMain()
		wv?.let { wv ->
			wv.loadUrl(url)
			return // Done. Skip code below.
		}
		if (!isDestroyed) initUrl = url
	}

	@MainThread
	private fun setUpWebView() {
		assertThreadMain()
		assert({ wv == null })

		val wv = WebView(this)
		wv.webViewClient = InternalWebViewClient(this)

		val ws = wv.settings
		@SuppressLint("SetJavaScriptEnabled")
		ws.javaScriptEnabled = true

		wv.loadUrl(initUrl.also { initUrl = null } ?: "")

		// TODO! Properly handle persistent web view state.
		//  - See, https://github.com/google/accompanist/issues/1178
		//  - See also, https://www.reddit.com/r/androiddev/comments/fqwohj/

		this.wv = wv
		setContentView(wv)
	}

	private class InternalWebViewClient(
		private val activity: WvWindowActivity,
	) : WebViewClientCompat() {
		override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
			if (!request.isForMainFrame || !request.hasGesture()) {
				return false
			}
			// See, https://developer.android.com/develop/ui/views/layout/webapps/webview#HandlingNavigation
			activity.startActivity(Intent(Intent.ACTION_VIEW, request.url))
			return true
		}
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent != null) window?.let { w ->
			val busId = WvWindowHandle.getPostBusId(intent) ?: return@let
			val payload = WvWindowHandle.getPostPayload(intent) ?: return@let
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
				detachPeer() // So that `finishAndRemoveTask()` isn't called by `close()` below
				close()
			}
			window?.onDestroy() // May throw
		}
		super.onDestroy()
	}
}
