package kokoro.jcef

import androidx.annotation.EmptySuper
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefCookieAccessFilter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import org.cef.network.CefURLRequest

abstract class JcefRequestHandlerAdapter : CefRequestHandlerAdapter(), CefResourceRequestHandler {

	override fun getResourceRequestHandler(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
		isNavigation: Boolean,
		isDownload: Boolean,
		requestInitiator: String?,
		disableDefaultHandling: BoolRef?,
	): CefResourceRequestHandler = this

	@EmptySuper
	override fun getCookieAccessFilter(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
	): CefCookieAccessFilter? = null

	@EmptySuper
	override fun onBeforeResourceLoad(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
	): Boolean = false

	@EmptySuper
	override fun getResourceHandler(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
	): CefResourceHandler? = null

	@EmptySuper
	override fun onResourceRedirect(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
		response: CefResponse?,
		new_url: StringRef?,
	) = Unit

	@EmptySuper
	override fun onResourceResponse(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
		response: CefResponse?,
	): Boolean = false

	@EmptySuper
	override fun onResourceLoadComplete(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
		response: CefResponse?,
		status: CefURLRequest.Status?,
		receivedContentLength: Long,
	) = Unit

	@EmptySuper
	override fun onProtocolExecution(
		browser: CefBrowser?,
		frame: CefFrame?,
		request: CefRequest?,
		allowOsExecution: BoolRef?,
	) = Unit
}
