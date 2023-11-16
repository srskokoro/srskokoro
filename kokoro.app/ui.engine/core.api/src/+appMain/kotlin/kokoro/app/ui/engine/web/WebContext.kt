package kokoro.app.ui.engine.web

interface WebContext : WebRequestHandler {

	/**
	 * Called whenever this [WebContext] will be used to handle a [web request][onWebRequest]
	 * and [sourceOrigin] can be determined from the current frame or web
	 * request. A `false` return indicates that usage of this [WebContext]
	 * should be denied, and possibly causing the engine to simply fail the
	 * request.
	 *
	 * CONTRACT: This method should not be called if [sourceOrigin] is the same
	 * as the target origin of the request.
	 *
	 * The engine might skip calling this method on certain occasions, such as
	 * when the value for [sourceOrigin] cannot be determined. When this method
	 * is not called, it's up to the engine to decide whether or not to forbid
	 * usage of this [WebContext] for handling the request.
	 */
	fun shouldAllowUsageFromOrigin(sourceOrigin: WebOrigin): Boolean

	override fun onWebRequest(request: WebRequest): WebResponse?

	suspend fun onJsMessage(what: Int, data: String): String
}
