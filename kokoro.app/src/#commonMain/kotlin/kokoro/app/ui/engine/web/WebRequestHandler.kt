package kokoro.app.ui.engine.web

fun interface WebRequestHandler {

	suspend fun handle(request: WebRequest): WebResponse
}
