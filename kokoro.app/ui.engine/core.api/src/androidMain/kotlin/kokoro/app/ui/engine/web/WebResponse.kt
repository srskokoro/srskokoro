package kokoro.app.ui.engine.web

import android.webkit.WebResourceResponse
import assert
import kokoro.internal.io.asInputStream
import okio.Source
import java.util.Collections.singletonMap

actual class WebResponse {
	val platformValue: WebResourceResponse

	actual val status: Int get() = platformValue.statusCode.let { if (it == 0) 200 else it }
	actual val mimeType: String? get() = platformValue.mimeType
	actual val encoding: String? get() = platformValue.encoding
	actual val headers: Map<String, String>
	actual val contentLength: Long
	actual val content: Source

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		headers: Map<String, String>,
		contentLength: Long,
		content: Source,
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* statusCode = */ status,
			/* reasonPhrase = */ getStatusMessage(status),
			/* responseHeaders = */
			buildMap {
				for ((k, v) in headers) put(k.lowercase(), v)
				if (contentLength >= 0) setContentLengthHeader(contentLength)
			},
			/* data = */ content.asInputStream(),
		)
		this.headers = headers
		this.contentLength = contentLength
		this.content = content
	}

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		contentLength: Long,
		content: Source,
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* data = */ content.asInputStream(),
		).also {
			it.setStatusCodeAndReasonPhrase(status, getStatusMessage(status))
			if (contentLength >= 0) it.setContentLengthAsLoneHeader(contentLength)
		}
		this.headers = emptyMap()
		this.contentLength = contentLength
		this.content = content
	}

	actual constructor(
		mimeType: String?,
		encoding: String?,
		contentLength: Long,
		content: Source,
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* data = */ content.asInputStream(),
		).also {
			if (contentLength >= 0) it.setContentLengthAsLoneHeader(contentLength)
		}
		this.headers = emptyMap()
		this.contentLength = contentLength
		this.content = content
	}

	actual companion object
}

private fun getStatusMessage(status: Int): String {
	// TODO Properly provide?
	return status.toString()
}

private fun WebResourceResponse.setContentLengthAsLoneHeader(dataLength: Long) {
	assert { dataLength >= 0 }
	responseHeaders = singletonMap("content-length", dataLength.toString())
}

private fun MutableMap<String, String>.setContentLengthHeader(dataLength: Long) {
	assert { dataLength >= 0 }
	put("content-length", dataLength.toString())
}
