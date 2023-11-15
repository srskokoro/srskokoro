package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

fun interface WebRequestHandler {

	fun onWebRequest(request: WebRequest): WebResponse?

	companion object {
		@JvmField val NULL = WebRequestHandler { null }
	}
}
