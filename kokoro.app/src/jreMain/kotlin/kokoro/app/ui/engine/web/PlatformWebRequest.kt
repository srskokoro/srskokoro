package kokoro.app.ui.engine.web

import org.cef.network.CefRequest

class PlatformWebRequest(
	// NOTE: Must not retain a reference to `CefRequest`, since it's often only
	// valid within the scope of the calling method in which it was provided.
	impl: CefRequest,
	override val url: WebUri,
) : BaseWebRequest() {
	constructor(impl: CefRequest) : this(impl, WebUri(impl.url))

	override val method: String = impl.method

	override fun headers() = headers_
	override fun header(name: String) = headers_[name.lowercase()]

	private var headers_ = buildMap {
		buildMap<String, String> {
			impl.getHeaderMap(this)
		}.mapKeysTo(this) {
			it.key.lowercase()
		}
	}
}
