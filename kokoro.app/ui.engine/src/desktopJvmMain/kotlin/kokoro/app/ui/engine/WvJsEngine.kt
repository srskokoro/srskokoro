package kokoro.app.ui.engine

import org.cef.browser.CefBrowser

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias WvJsEngine = CefBrowser

@Suppress("NOTHING_TO_INLINE")
actual inline fun WvJsEngine.evaluateJs(script: String) {
	executeJavaScript(script, null, 0)
}
