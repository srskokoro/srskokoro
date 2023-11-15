package kokoro.app.ui.engine.web

interface WebRequest {

	val method: String

	val url: WebUri

	fun headers(): Map<String, String>

	fun header(name: String): String?

	/**
	 * Indicates whether or not this object is still valid.
	 *
	 * When `false`, the methods and properties of this object may throw. It's
	 * recommended to clear references to this object in that case, so as to
	 * allow garbage collection and release any native resources.
	 */
	val isValid: Boolean
}
