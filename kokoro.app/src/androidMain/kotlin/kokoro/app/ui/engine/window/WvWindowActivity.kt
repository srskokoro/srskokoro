package kokoro.app.ui.engine.window

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.activity.ComponentActivity
import kokoro.app.ui.engine.web.WebUriResolver
import kokoro.app.ui.engine.window.webkit.WebViewClientImpl
import kokoro.internal.DEBUG
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.checkNotNull
import kokoro.internal.os.SerializationEncoded
import kotlinx.coroutines.CoroutineScope
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

		private const val SS_webView = "webView"
		private const val SS_oldStateEntries = "oldStateEntries"

		private fun <T> WvWindowBusBinding<*, T>.route(
			window: WvWindow, encoded: SerializationEncoded,
		) {
			route(window) { bus -> encoded.decode(bus.serialization) }
		}

		init {
			val cm = CookieManager.getInstance()
			// See also, https://stackoverflow.com/q/5404274
			cm.setAcceptCookie(false)
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

			val isInitialState: Boolean
			val webViewState: Bundle?
			val oldStateEntries: Bundle

			if (savedInstanceState != null) {
				isInitialState = false
				webViewState = savedInstanceState.getBundle(SS_webView) // Null if `WebView.saveState()` fails
				oldStateEntries = savedInstanceState.getBundle(SS_oldStateEntries) ?: kotlin.run {
					if (DEBUG) throw NullPointerException(::SS_oldStateEntries.name)
					Bundle.EMPTY
				}
			} else {
				isInitialState = true
				webViewState = null
				oldStateEntries = Bundle.EMPTY
			}

			val wc = WvContextImpl(h, this, oldStateEntries)
			val w = f.init(wc, isInitialState) // May throw
			window = w

			wc.scope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
				val wur = w.initWebUriResolver() // NOTE: Suspending call
				setUpWebView(wur, w.context.scope, webViewState)
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
	private fun setUpWebView(wur: WebUriResolver, scope: CoroutineScope, webViewState: Bundle?) {
		assertThreadMain()
		assert({ wv == null })

		val wv = WebView(this)
		if (webViewState != null) {
			// Implementation reference:
			// - https://github.com/KevinnZou/compose-webview/blob/0.33.6/web/src/main/java/com/kevinnzou/web/WebView.kt#L207
			// - https://github.com/google/accompanist/pull/1557
			wv.restoreState(webViewState)
		}

		wv.webViewClient = WebViewClientImpl(this, wur, scope)

		val ws = wv.settings
		@SuppressLint("SetJavaScriptEnabled")
		ws.javaScriptEnabled = true

		// See, https://stackoverflow.com/q/9819325
		// - See also, https://stackoverflow.com/q/5404274
		assert({ !ws.domStorageEnabled }) { "Web storage should've been disabled by default (according to the docs)." }
		assert({ !CookieManager.getInstance().acceptCookie() }) { "Cookie persistence should've been disabled already by the static initializer." }

		val initUrl = this.initUrl
		if (initUrl != null) {
			this.initUrl = null
			wv.loadUrl(initUrl)
		}

		this.wv = wv
		setContentView(wv)
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
		// Implementation reference: `android.webkit.WebViewFragment` (deprecated)
		// - See also, https://github.com/commonsguy/cw-omnibus/blob/v9.0/NFC/WebBeam/app/src/main/java/com/commonsware/android/webbeam/WebViewFragment.java
		wv?.onResume()
		super.onResume()
		window?.onResume()
	}

	override fun onPause() {
		window?.onPause()
		super.onPause()
		// Implementation reference: `android.webkit.WebViewFragment` (deprecated)
		// - See also, https://github.com/commonsguy/cw-omnibus/blob/v9.0/NFC/WebBeam/app/src/main/java/com/commonsware/android/webbeam/WebViewFragment.java
		wv?.onPause()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		window?.run {
			onSaveState()
			(context as? WvContextImpl)
		}?.run {
			val o = encodeStateEntries()
			outState.putBundle(SS_oldStateEntries, o)
		}
		wv?.run {
			val o = Bundle()
			if (saveState(o) != null)
				outState.putBundle(SS_webView, o)
		}
	}

	override fun onDestroy() {
		if (isFinishing) handle?.run {
			detachPeer() // So that `finishAndRemoveTask()` isn't called by `close()` below
			close()
		}

		window?.onDestroy() // May throw

		// Implementation reference: `android.webkit.WebViewFragment` (deprecated)
		// - See also, https://github.com/commonsguy/cw-omnibus/blob/v9.0/NFC/WebBeam/app/src/main/java/com/commonsware/android/webbeam/WebViewFragment.java
		// - Perhaps see also, https://stackoverflow.com/q/3130654
		wv?.run {
			wv = null
			val parent = parent
			if (parent is ViewGroup)
				parent.removeView(this)
			destroy()
		}

		super.onDestroy()
	}
}
