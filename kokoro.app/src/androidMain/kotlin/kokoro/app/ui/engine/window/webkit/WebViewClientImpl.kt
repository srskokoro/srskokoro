package kokoro.app.ui.engine.window.webkit

import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewClientCompat
import kokoro.app.ui.engine.web.PlatformWebRequest
import kokoro.app.ui.engine.web.WebUri
import kokoro.app.ui.engine.web.WebUriResolver
import kokoro.app.ui.engine.web.toWebkit
import kokoro.app.ui.engine.window.WvWindowActivity
import kokoro.app.ui.engine.window.nook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@nook internal class WebViewClientImpl(
	private val activity: WvWindowActivity,
	private val wur: WebUriResolver,
	scope: CoroutineScope,
) : WebViewClientCompat() {
	private val coroutineContext = scope.coroutineContext + Dispatchers.IO

	override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
		val h = wur.resolve(WebUri(request.url))
		if (h != null) {
			return runBlocking(coroutineContext) {
				h.apply(PlatformWebRequest(request)).toWebkit()
			}
		}
		return null
	}

	override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
		if (!request.isForMainFrame || !request.hasGesture()) {
			// TIP: See also `Sec-Fetch-User` request header -- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-User
			return false
		}
		// See, https://developer.android.com/develop/ui/views/layout/webapps/webview#HandlingNavigation
		activity.startActivity(Intent(Intent.ACTION_VIEW, request.url))
		return true
	}
}
