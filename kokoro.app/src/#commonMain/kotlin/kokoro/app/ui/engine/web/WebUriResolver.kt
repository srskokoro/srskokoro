package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

fun interface WebUriResolver {

	fun resolve(uri: WebUri): WebRequestHandler?

	companion object {
		@JvmField val NULL = WebUriResolver { null }
	}

	operator fun plus(other: WebUriResolver): WebUriResolver =
		WebUriResolver { resolve(it) ?: other.resolve(it) }
}
