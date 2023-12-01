package kokoro.app.ui.engine

import android.webkit.WebView

actual typealias WvJsEngine = WebView

@Suppress("NOTHING_TO_INLINE")
actual inline fun WvJsEngine.evaluateJs(script: String) {
	evaluateJavascript(script, null)
}
