package kokoro.app.ui.engine.web

interface WebRequest {

	val isUiEngineRequest: Boolean

	val referrer: WebUri

	val method: String

	val url: WebUri

	fun headers(): Map<String, String>

	fun header(name: String): String?

	var isSwitchingContext: Boolean
}
