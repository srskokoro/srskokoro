package kokoro.app.ui.engine.web

interface WebRequest {

	val method: String

	val url: WebUri

	fun headers(): Map<String, String>

	fun header(name: String): String?
}
