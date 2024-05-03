package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

fun interface WebRequestResolver {

	fun findHandler(url: WebUri): WebRequestHandler?

	companion object {
		@JvmField val NULL = WebRequestResolver { null }
	}
}
