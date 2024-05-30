package kokoro.app.ui.engine.window.jcef

import kokoro.app.ui.engine.web.WebUriResolver
import kokoro.app.ui.engine.window.nook
import kokoro.internal.DEBUG
import kotlinx.coroutines.CoroutineScope
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import java.awt.Desktop
import java.net.URI

@nook internal class CefRequestHandlerImpl(
	wur: WebUriResolver,
	scope: CoroutineScope,
) : CefRequestHandlerAdapter() {
	private val navigationResourceRequestHandler = CefResourceRequestHandlerImpl(isNavigation = true, wur, scope)
	private val generalResourceRequestHandler = CefResourceRequestHandlerImpl(isNavigation = false, wur, scope)

	override fun getResourceRequestHandler(
		browser: CefBrowser?, frame: CefFrame?, request: CefRequest?,
		isNavigation: Boolean, isDownload: Boolean,
		requestInitiator: String?, disableDefaultHandling: BoolRef?,
	): CefResourceRequestHandler =
		if (isNavigation) navigationResourceRequestHandler
		else generalResourceRequestHandler

	// --

	private fun launchUrlExternally(url: String) {
		if (Desktop.isDesktopSupported()) try {
			val desktop = Desktop.getDesktop()
			if (desktop.isSupported(Desktop.Action.BROWSE))
				desktop.browse(URI(url))
		} catch (ex: Throwable) {
			if (DEBUG) throw ex
			ex.printStackTrace()
		}
	}

	override fun onBeforeBrowse(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
		user_gesture: Boolean,
		is_redirect: Boolean,
	): Boolean {
		if (frame == null || !frame.isMain || !user_gesture) {
			// TIP: See also `Sec-Fetch-User` request header -- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-User
			return false
		}
		if (request != null) {
			launchUrlExternally(request.url)
		}
		return true // Override default behavior
	}

	override fun onOpenURLFromTab(
		browser: CefBrowser?,
		frame: CefFrame?,
		target_url: String?,
		user_gesture: Boolean,
	): Boolean {
		if (target_url != null) {
			launchUrlExternally(target_url)
		}
		return true // Override default behavior
	}
}
