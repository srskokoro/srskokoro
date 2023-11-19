package kokoro.app.ui.engine.web

class WebOrigin {
	val scheme: String
	val host: String
	val port: Int

	constructor(
		scheme: String,
		host: String,
		port: Int,
	) {
		this.scheme = scheme
		this.host = host
		this.port =
			if (port >= 0) port
			else WebUri.getPortForScheme(scheme)
	}

	constructor(
		scheme: String,
		host: String,
	) {
		this.scheme = scheme
		this.host = host
		this.port = WebUri.getPortForScheme(scheme)
	}

	companion object {
		@Suppress("NOTHING_TO_INLINE")
		inline fun fromUri(uri: WebUri) = WebOrigin(
			scheme = uri.scheme() ?: "",
			host = uri.host() ?: "",
			// `raw` since we do our own normalization already
			port = uri.port(raw = true),
		)

		@Suppress("NOTHING_TO_INLINE")
		inline fun fromUri(uri: WebUriValue) = fromUri(WebUri(uri))

		@Suppress("NOTHING_TO_INLINE")
		inline fun fromUri(uri: String) = fromUri(WebUri(uri))
	}

	override fun toString() = buildString {
		// See, “6.2. ASCII Serialization of an Origin | RFC 6454”
		// -- https://datatracker.ietf.org/doc/html/rfc6454#autoid-22
		val scheme = scheme
		append(scheme)
		append("://")
		append(host)
		val port = port
		if (port != WebUri.getPortForScheme(scheme) && port >= 0) {
			append(':')
			append(port)
		}
	}

	override fun hashCode(): Int {
		var h = scheme.hashCode()
		h = h * 31 + host.hashCode()
		h = h * 31 + port
		return h
	}

	override fun equals(other: Any?) =
		if (this !== other) {
			other is WebOrigin
				&& scheme == other.scheme
				&& host == other.host
				&& port == other.port
		} else true
}
