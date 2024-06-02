package kokoro.app.ui.engine.web

import android.webkit.WebResourceRequest

class PlatformWebRequest(
	private val impl: WebResourceRequest,
) : BaseWebRequest() {

	override val url: WebUri get() = WebUri(impl.url)

	override val method: String get() = impl.method

	override fun headers(): Map<String, String> = impl.requestHeaders

	override fun header(name: String): String? = impl.requestHeaders[name]
}
