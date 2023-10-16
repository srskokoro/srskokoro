package kokoro.app.ui.wv

import org.cef.browser.CefBrowser

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias JsEvaluator = CefBrowser

@Suppress("NOTHING_TO_INLINE")
actual inline fun JsEvaluator.evaluateJs(script: String) {
	executeJavaScript(script, null, 0)
}
