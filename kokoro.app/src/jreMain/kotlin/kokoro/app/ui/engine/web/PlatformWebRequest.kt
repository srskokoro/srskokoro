package kokoro.app.ui.engine.web

import org.cef.network.CefRequest

class PlatformWebRequest(
	// NOTE: Must not retain a reference to `CefRequest`, since it's often only
	// valid within the scope of the calling method in which it was provided.
	impl: CefRequest,
) : WebRequest {

	override val method: String = impl.method
	override val url = WebUri(impl.url)

	override fun headers() = headers_
	override fun header(name: String) = headers_[name]

	private var headers_ = buildMap<String, String> {
		impl.getHeaderMap(this)
	}
}
