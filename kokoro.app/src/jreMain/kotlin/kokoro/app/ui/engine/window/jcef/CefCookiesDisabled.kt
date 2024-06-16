package kokoro.app.ui.engine.window.jcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefCookieAccessFilter
import org.cef.network.CefCookie
import org.cef.network.CefRequest
import org.cef.network.CefResponse

internal object CefCookiesDisabled : CefCookieAccessFilter {

	override fun canSendCookie(
		browser: CefBrowser?, frame: CefFrame?,
		request: CefRequest?, cookie: CefCookie?,
	): Boolean = false // Disables cookies

	override fun canSaveCookie(
		browser: CefBrowser?, frame: CefFrame?,
		request: CefRequest?, response: CefResponse?, cookie: CefCookie?,
	): Boolean = false // Disables cookies
}
