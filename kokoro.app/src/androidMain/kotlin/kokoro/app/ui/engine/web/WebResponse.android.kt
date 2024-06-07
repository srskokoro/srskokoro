package kokoro.app.ui.engine.web

import android.webkit.WebResourceResponse
import kokoro.internal.io.asInputStream

fun WebResponse.toWebkit(): WebResourceResponse {
	val contentLength = contentLength
	var headers: Map<String, String> = headers
	headers = buildMap(headers.size + 1) {
		putAll(headers)
		if (contentLength >= 0)
			put("Content-Length", contentLength.toString())
	}
	val status = status
	return WebResourceResponse(
		/* mimeType = */ mimeType,
		/* encoding = */ charset,
		/* statusCode = */ status,
		/* reasonPhrase = */ status.toString(), // TODO Properly provide?
		/* responseHeaders = */ headers,
		/* data = */ content.asInputStream(),
	)
}
