package kokoro.app.ui.engine.web

import androidx.annotation.CallSuper
import kokoro.internal.io.asClearing
import okio.Buffer

abstract class BasePlatformJsResource : WebResource {

	override suspend fun apply(request: WebRequest): WebResponse {
		request.header("sec-fetch-dest")?.let {
			if (it != "script") return WebResponse(403)
		}
		val buffer = Buffer()
		feed(buffer)
		return WebResponse(
			mimeType = "text/javascript",
			charset = "utf-8",
			buffer.size, buffer.asClearing(),
		)
	}

	@CallSuper
	open fun feed(out: Buffer) {
		out.writeUtf8("HTTPX='")
		out.writeUtf8(HTTPX)
		out.writeUtf8("'\n")
	}
}
