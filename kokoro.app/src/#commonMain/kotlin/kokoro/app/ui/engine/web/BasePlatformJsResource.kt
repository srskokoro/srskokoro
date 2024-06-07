package kokoro.app.ui.engine.web

import androidx.annotation.CallSuper
import kokoro.internal.io.asClearing
import okio.Buffer

abstract class BasePlatformJsResource : WebResource {

	override suspend fun apply(request: WebRequest): WebResponse {
		val buffer = Buffer()
		feed(buffer, request)
		return WebResponse(
			mimeType = "text/javascript",
			charset = "utf-8",
			buffer.size, buffer.asClearing(),
		)
	}

	@CallSuper
	open fun feed(out: Buffer, request: WebRequest) {
		out.writeUtf8("HTTPX='")
		out.writeUtf8(HTTPX)
		out.writeUtf8("'\n")
	}
}
