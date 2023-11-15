package kokoro.app.ui.engine.web

import android.webkit.WebResourceResponse
import kokoro.internal.io.asInputStream
import okio.Source

actual class WebResponse {
	val platformValue: WebResourceResponse

	actual val status: Int get() = platformValue.statusCode.let { if (it == 0) 200 else it }
	actual val mimeType: String? get() = platformValue.mimeType
	actual val encoding: String? get() = platformValue.encoding
	actual val headers: Map<String, String> get() = platformValue.responseHeaders ?: emptyMap()
	actual val data: Source

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		headers: Map<String, String>,
		data: Source,
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* statusCode = */ status,
			/* reasonPhrase = */ getStatusMessage(status),
			/* responseHeaders = */ headers,
			/* data = */ data.asInputStream(),
		)
		this.data = data
	}

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		data: Source
	) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* data = */ data.asInputStream(),
		).also {
			it.setStatusCodeAndReasonPhrase(status, getStatusMessage(status))
		}
		this.data = data
	}

	actual constructor(mimeType: String?, encoding: String?, data: Source) {
		platformValue = WebResourceResponse(
			/* mimeType = */ mimeType,
			/* encoding = */ encoding,
			/* data = */ data.asInputStream(),
		)
		this.data = data
	}

	actual companion object
}

private fun getStatusMessage(status: Int): String {
	// TODO Properly provide?
	return status.toString()
}
