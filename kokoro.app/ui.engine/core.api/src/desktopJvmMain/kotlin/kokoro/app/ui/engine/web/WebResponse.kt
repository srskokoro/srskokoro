package kokoro.app.ui.engine.web

import okio.Source

actual class WebResponse actual constructor(
	actual val status: Int,
	actual val mimeType: String?,
	actual val encoding: String?,
	actual val headers: Map<String, String>,
	actual val contentLength: Long,
	actual val content: Source,
) {
	init {
		WebResponse_checkStatus_nonAndroid(status)
	}

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		contentLength: Long,
		content: Source,
	) : this(
		status = status,
		mimeType = mimeType,
		encoding = encoding,
		headers = emptyMap(),
		contentLength = contentLength,
		content,
	)

	actual constructor(
		mimeType: String?,
		encoding: String?,
		contentLength: Long,
		content: Source,
	) : this(
		status = 200,
		mimeType = mimeType,
		encoding = encoding,
		contentLength = contentLength,
		content,
	)

	actual companion object
}
