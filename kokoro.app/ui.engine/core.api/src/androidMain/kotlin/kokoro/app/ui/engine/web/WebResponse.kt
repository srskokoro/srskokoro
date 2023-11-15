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
	actual val dataLength: Long
	actual val data: Source

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		headers: Map<String, String>,
		dataLength: Long,
		data: Source,
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* statusCode = */ status,
			/* reasonPhrase = */ getStatusMessage(status),
			/* responseHeaders = */
			buildMap {
				for ((k, v) in headers) put(k.lowercase(), v)
				if (dataLength >= 0) setDataLengthHeader(dataLength)
			},
			/* data = */ data.asInputStream(),
		)
		this.headers = headers
		this.dataLength = dataLength
		this.data = data
	}

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		dataLength: Long,
		data: Source,
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* data = */ data.asInputStream(),
		).also {
			it.setStatusCodeAndReasonPhrase(status, getStatusMessage(status))
			if (dataLength >= 0) it.setDataLengthAsLoneHeader(dataLength)
		}
		this.headers = emptyMap()
		this.dataLength = dataLength
		this.data = data
	}

	actual constructor(
		mimeType: String?,
		encoding: String?,
		dataLength: Long,
		data: Source,
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* data = */ data.asInputStream(),
		).also {
			if (dataLength >= 0) it.setDataLengthAsLoneHeader(dataLength)
		}
		this.headers = emptyMap()
		this.dataLength = dataLength
		this.data = data
	}

	actual companion object
}

private fun getStatusMessage(status: Int): String {
	// TODO Properly provide?
	return status.toString()
}

private fun WebResourceResponse.setDataLengthAsLoneHeader(dataLength: Long) {
	assert { dataLength >= 0 }
	responseHeaders = singletonMap("content-length", dataLength.toString())
}

private fun MutableMap<String, String>.setDataLengthHeader(dataLength: Long) {
	assert { dataLength >= 0 }
	put("content-length", dataLength.toString())
}
