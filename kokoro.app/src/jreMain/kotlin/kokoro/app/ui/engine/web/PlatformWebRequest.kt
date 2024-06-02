package kokoro.app.ui.engine.web

import org.cef.network.CefRequest
import kotlin.collections.MutableMap.MutableEntry

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

	@Suppress("RemoveExplicitTypeArguments")
	private var headers_ = buildMap<String, String> {
		impl.getHeaderMap(object : AbstractMutableMap<String, String>() {
			override fun put(key: String, value: String) = this@buildMap.put(key.lowercase(), value)
			override val entries: MutableSet<MutableEntry<String, String>> get() = this@buildMap.entries
		})
	}
}
