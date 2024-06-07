package kokoro.app.ui.engine.web

import android.webkit.WebResourceRequest

class PlatformWebRequest(
	private val impl: WebResourceRequest,
) : BaseWebRequest() {

	override val url: WebUri get() = WebUri(impl.url)

	override val method: String get() = impl.method

	override fun headers() = headers_
	override fun header(name: String) = headers_[name.lowercase()]

	private var headers_ = buildMap<String, String> {
		val requestHeaders = impl.requestHeaders
		// NOTE: At the time of writing, `requestHeaders` is just an ordinary
		// `HashMap` and its keys are case-sensitive. Thus, this special setup
		// is necessary.
		for ((name, value) in requestHeaders) put(name.lowercase(), value)
	}
}
