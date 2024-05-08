package kokoro.app.ui.engine.web

import kokoro.internal.io.VoidSource
import kotlin.jvm.JvmField

fun interface WebResource {

	suspend fun apply(request: WebRequest): WebResponse

	companion object {
		@JvmField val EMPTY = WebResource()
	}
}

/** @see WebResource.EMPTY */
fun WebResource(): WebResource = EmptyWebResource()

private class EmptyWebResource : WebResource {
	override suspend fun apply(request: WebRequest) =
		WebResponse("text/plain", null, 0, VoidSource)
}
