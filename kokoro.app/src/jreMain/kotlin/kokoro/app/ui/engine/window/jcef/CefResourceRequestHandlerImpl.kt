package kokoro.app.ui.engine.window.jcef

import kokoro.app.ui.engine.web.PlatformWebRequest
import kokoro.app.ui.engine.web.WebUri
import kokoro.app.ui.engine.web.WebUriResolver
import kotlinx.coroutines.CoroutineScope
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefCookieAccessFilter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.network.CefRequest

internal class CefResourceRequestHandlerImpl(
	private val isNavigation: Boolean,
	private val wur: WebUriResolver,
	private val scope: CoroutineScope,
) : CefResourceRequestHandlerAdapter() {

	override fun getResourceHandler(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): CefResourceHandler? {
		if (request != null) {
			val uri = WebUri(request.url)
			val h = wur.resolve(uri)
			if (h != null) return CefResourceHandlerImpl(
				PlatformWebRequest(request, uri),
				isNavigation = isNavigation,
				h, scope,
			)
		}
		return null
	}

	override fun getCookieAccessFilter(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): CefCookieAccessFilter {
		return CefCookiesDisabled
	}
}
