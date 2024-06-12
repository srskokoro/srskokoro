package kokoro.app.ui.engine.window

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.core.os.BundleCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kokoro.app.ui.engine.UiStatesParcelable
import kokoro.app.ui.engine.UiStatesSaver
import kokoro.app.ui.engine.web.HOST_X
import kokoro.app.ui.engine.web.HTTPX
import kokoro.app.ui.engine.web.WebUriResolver
import kokoro.app.ui.engine.window.webkit.WebViewClientImpl
import kokoro.internal.DEBUG
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.assertUnreachable
import kokoro.internal.checkNotNull
import kokoro.internal.os.SerializationEncoded
import kokoro.internal.os.taskIdCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(nook::class)
class WvWindowActivity : ComponentActivity() {

	companion object {

		private const val SS_handleId = "handleId"

		private const val SS_webView = "webView"
		private const val SS_oldStateEntries = "oldStateEntries"
		private const val SS_oldUiStates = "oldUiStates"

		private fun <T> WvWindowBusBinding<*, T>.route(
			window: WvWindow, encoded: SerializationEncoded,
		) {
			route(window) { bus -> encoded.decode(bus.serialization) }
		}
	}

	private var handle: WvWindowHandle? = null
	private var task: Any? = null
	private var window: WvWindow? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		run<Unit> {
			val h = (if (savedInstanceState != null) {
				savedInstanceState.getString(SS_handleId)
					?.let { WvWindowHandle.get(it) }
			} else {
				val intent = intent
				if (intent.action != WvWindowHandle.ACTION_LAUNCH) return@run
				WvWindowHandle.get(intent)
			}) ?: return@run

			val fid = h.windowFactoryId
			val f = checkNotNull(WvWindowFactory.get(fid), or = {
				"No factory registered for window factory ID: $fid"
			})

			val taskId = taskId
			val t = h.peer_.let { c ->
				when (c) {
					null -> return@let taskId
					is Activity -> {} // Fail
					is ActivityManager.AppTask -> {
						if (c.taskInfo.taskIdCompat == taskId) return@let c
					}
					is Int -> {
						if (c == taskId) return@let c
					}
					else -> throw AssertionError("Unexpected: $c")
				}
				assertUnreachable { "Handle already has a peer: $h" }
				return@run
			}
			h.attachPeer(this)
			task = t
			handle = h

			val isInitialState: Boolean
			val webViewState: Bundle?
			val oldStateEntries: Bundle
			val oldUiStates: UiStatesParcelable

			if (savedInstanceState != null) {
				isInitialState = false
				webViewState = savedInstanceState.getBundle(SS_webView) // Null if `WebView.saveState()` fails
				oldStateEntries = savedInstanceState.getBundle(SS_oldStateEntries) ?: kotlin.run {
					if (DEBUG) throw NullPointerException(::SS_oldStateEntries.name)
					Bundle.EMPTY
				}
				oldUiStates = BundleCompat.getParcelable(savedInstanceState, SS_oldUiStates, UiStatesParcelable::class.java) ?: kotlin.run {
					if (DEBUG) throw NullPointerException(::SS_oldUiStates.name)
					UiStatesParcelable()
				}
			} else {
				isInitialState = true
				webViewState = null
				oldStateEntries = Bundle.EMPTY
				oldUiStates = UiStatesParcelable()
			}

			val wc = WvContextImpl(h, this, oldStateEntries)
			val w = f.init(wc, isInitialState) // May throw
			window = w

			wc.scope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
				val wur = w.initWebUriResolver() // NOTE: Suspending call
				setUpWebView(wur, w.context.scope, webViewState, oldUiStates)
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

	private var uiSs: UiStatesSaver? = null

	@MainThread
	fun loadUrl(url: String) {
		assertThreadMain()
		wv?.let { wv ->
			wv.loadUrl(url)
			return // Done. Skip code below.
		}
		if (!isDestroyed) initUrl = url
	}

	private object WebView_globalInit {
		init {
			// TODO Let the user be able to toggle this on/off through some kind of app preferences
			if (DEBUG) WebView.setWebContentsDebuggingEnabled(true)

			val cm = CookieManager.getInstance()
			// See also, https://stackoverflow.com/q/5404274
			cm.setAcceptCookie(false)
		}
	}

	@MainThread
	private fun setUpWebView(wur: WebUriResolver, scope: CoroutineScope, webViewState: Bundle?, oldUiStates: UiStatesParcelable) {
		assertThreadMain()
		assert({ wv == null })

		WebView_globalInit // Force static initialization

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

		if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
			val uiSs = UiStatesSaver(oldUiStates.map)
			this.uiSs = uiSs
			wv.addJavascriptInterface(uiSs, UiStatesSaver.JSI__name)
			WebViewCompat.addWebMessageListener(wv, UiStatesSaver.WML__name, setOf("$HTTPX://*.$HOST_X"), uiSs)
		} else {
			// TODO! Instruct the user to upgrade their web view
			throw UnsupportedOperationException(WebViewFeature.WEB_MESSAGE_LISTENER)
		}

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
		handle?.let { h ->
			outState.putString(SS_handleId, h.id)
		}
		window?.run {
			onSaveState()
			(context as? WvContextImpl)
		}?.run {
			val o = encodeStateEntries()
			outState.putBundle(SS_oldStateEntries, o)
		}
		wv?.run {
			uiSs?.let { outState.putParcelable(SS_oldUiStates, it.encode()) }
			val o = Bundle()
			if (saveState(o) != null)
				outState.putBundle(SS_webView, o)
		}
	}

	override fun onDestroy() {
		handle?.let { h ->
			if (isFinishing) {
				h.detachPeer() // So that `finishAndRemoveTask()` isn't called by `close()` below
				h.close()
			} else {
				h.attachPeer(task)
			}
		}

		window?.onDestroy() // May throw

		// Implementation reference: `android.webkit.WebViewFragment` (deprecated)
		// - See also, https://github.com/commonsguy/cw-omnibus/blob/v9.0/NFC/WebBeam/app/src/main/java/com/commonsware/android/webbeam/WebViewFragment.java
		// - Perhaps see also, https://stackoverflow.com/q/17418503
		wv?.run {
			wv = null
			uiSs = null
			val parent = parent
			if (parent is ViewGroup)
				parent.removeView(this)
			destroy()
		}

		super.onDestroy()
	}
}
