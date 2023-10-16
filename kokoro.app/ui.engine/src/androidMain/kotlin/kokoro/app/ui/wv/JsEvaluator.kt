package kokoro.app.ui.wv

import android.webkit.WebView

actual typealias JsEvaluator = WebView

@Suppress("NOTHING_TO_INLINE")
actual inline fun JsEvaluator.evaluateJs(script: String) {
	evaluateJavascript(script, null)
}
