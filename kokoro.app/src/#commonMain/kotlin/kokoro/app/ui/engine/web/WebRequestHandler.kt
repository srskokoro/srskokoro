package kokoro.app.ui.engine.web

import kokoro.internal.io.VoidSource
import kotlin.jvm.JvmField

fun interface WebRequestHandler {

	suspend fun handle(request: WebRequest): WebResponse

	companion object {

		/** @see invoke */
		@JvmField val EMPTY = WebRequestHandler()

		/** @see EMPTY */
		operator fun invoke(): WebRequestHandler = EmptyWebRequestHandler()
	}

	private class EmptyWebRequestHandler : WebRequestHandler {
		override suspend fun handle(request: WebRequest) =
			WebResponse("text/plain", null, 0, VoidSource)
	}
}
