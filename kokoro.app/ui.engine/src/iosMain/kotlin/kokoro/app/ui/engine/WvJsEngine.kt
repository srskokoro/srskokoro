package kokoro.app.ui.engine

import platform.WebKit.WKWebView

actual typealias WvJsEngine = WKWebView

actual inline fun WvJsEngine.evaluateJs(script: String) {
	evaluateJavaScript(script, null)
}
