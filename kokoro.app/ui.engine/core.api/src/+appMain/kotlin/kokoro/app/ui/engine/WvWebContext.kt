package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.WebContext
import kokoro.app.ui.engine.web.WebOrigin
import kokoro.app.ui.engine.web.WebRequest
import kokoro.app.ui.engine.web.WebRequestHandler
import kokoro.app.ui.engine.web.WebResponse

abstract class WvWebContext(
	val interceptor: WebRequestHandler,
) : WebContext, WebRequestHandler, WvUnitIdMapper {

	constructor() : this(WebRequestHandler.NULL)

	companion object {
		init {
			WvWebContext_platformInit()
		}
	}

	abstract override fun shouldAllowUsageFromOrigin(sourceOrigin: WebOrigin): Boolean

	final override fun onWebRequest(request: WebRequest): WebResponse? {
		val response = interceptor.onWebRequest(request)
		if (response != null) return response

		return onHandleWebRequest(request)
	}

	abstract fun onHandleWebRequest(request: WebRequest): WebResponse?

	abstract override suspend fun onJsMessage(what: Int, data: String): String

	abstract override fun toWvUnitId(wvUnitKey: String): Int
}
