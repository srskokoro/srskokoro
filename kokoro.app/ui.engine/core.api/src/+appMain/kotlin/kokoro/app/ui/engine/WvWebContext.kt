package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.WebContext
import kokoro.app.ui.engine.web.WebRequest
import kokoro.app.ui.engine.web.WebRequestHandler
import kokoro.app.ui.engine.web.WebResponse

abstract class WvWebContext : WebContext, WebRequestHandler, WvUnitIdMapper {

	var fallbackWebRequestHandler = WebRequestHandler.NULL

	final override fun onWebRequest(request: WebRequest): WebResponse? {
		val response = onHandleWebRequest(request)
		if (response != null) return response

		return fallbackWebRequestHandler.onWebRequest(request)
	}

	abstract fun onHandleWebRequest(request: WebRequest): WebResponse?

	abstract override suspend fun onJsMessage(what: Int, data: String): String

	abstract override fun toWvUnitId(wvUnitKey: String): Int

	companion object {
		init {
			WvWebContext_platformInit()
		}
	}
}
