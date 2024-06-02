package kokoro.app.ui.engine.web

import android.webkit.WebResourceRequest

class PlatformWebRequest(
	private val impl: WebResourceRequest,
) : BaseWebRequest() {

	override val url: WebUri get() = WebUri(impl.url)

	override val method: String get() = impl.method

	// TODO! Confirm that the header names are already lowercase and that no further handling is needed
	override fun headers(): Map<String, String> = impl.requestHeaders

	// TODO! Confirm whether or not header name case is ignored on header entry query
	override fun header(name: String): String? = impl.requestHeaders[name]
}
