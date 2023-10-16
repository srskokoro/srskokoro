package kokoro.app.ui.wv

import platform.WebKit.WKWebView

actual typealias JsEvaluator = WKWebView

actual fun JsEvaluator.evaluateJs(script: String) {
	TODO("Not yet implemented")
}
