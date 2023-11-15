package kokoro.app.ui.engine.web

interface WebContext : WebRequestHandler {

	override fun onWebRequest(request: WebRequest): WebResponse?

	suspend fun onJsMessage(what: Int, data: String): String
}
