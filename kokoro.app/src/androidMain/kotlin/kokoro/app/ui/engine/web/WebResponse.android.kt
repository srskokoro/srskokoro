package kokoro.app.ui.engine.web

import android.webkit.WebResourceResponse
import kokoro.internal.io.asInputStream

fun WebResponse.toWebkit() = WebResourceResponse(
	/* mimeType = */ mimeType,
	/* encoding = */ charset,
	/* statusCode = */ status,
	/* reasonPhrase = */ status.toString(), // TODO Properly provide?
	/* responseHeaders = */
	buildMap(headers.size + 1) {
		putAll(headers)
		if (contentLength >= 0)
			put("content-length", contentLength.toString())
	},
	/* data = */ content.asInputStream(),
)
