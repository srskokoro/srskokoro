package kokoro.app.ui.engine.web

fun interface WebRequestHandler {

	fun onWebRequest(request: WebRequest): WebResponse?
}
