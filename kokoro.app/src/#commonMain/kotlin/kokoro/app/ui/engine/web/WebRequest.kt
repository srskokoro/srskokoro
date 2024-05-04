package kokoro.app.ui.engine.web

interface WebRequest {

	val url: WebUri

	val method: String

	fun headers(): Map<String, String>

	fun header(name: String): String?
}
