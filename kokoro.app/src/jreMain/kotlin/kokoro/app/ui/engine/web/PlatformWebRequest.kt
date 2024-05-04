package kokoro.app.ui.engine.web

import org.cef.network.CefRequest

class PlatformWebRequest(
	private val impl: CefRequest,
) : WebRequest {

	override val method: String get() = impl.method

	private var url_: WebUri? = null
	override val url: WebUri
		get() = url_ ?: WebUri(impl.url)
			.also { url_ = it }

	private var headers_: Map<String, String>? = null
	override fun headers(): Map<String, String> =
		headers_ ?: buildMap { impl.getHeaderMap(this) }
			.also { headers_ = it }

	override fun header(name: String): String? = impl.getHeaderByName(name)
}
