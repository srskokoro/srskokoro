package kokoro.app.ui.engine.web

import okio.Source

actual class WebResponse {
	actual val status: Int
	actual val mimeType: String?
	actual val charset: String?
	actual val headers: Map<String, String>
	actual val contentLength: Long
	actual val content: Source

	actual constructor(
		status: Int,
		mimeType: String?,
		charset: String?,
		headers: Map<String, String>,
		contentLength: Long,
		content: Source,
	) {
		WebResponse_checkStatus_nonAndroid(status)
		this.status = status
		this.mimeType = mimeType
		this.charset = charset
		this.headers = headers
		this.contentLength = contentLength
		this.content = content
	}

	actual constructor(
		status: Int,
		mimeType: String?,
		charset: String?,
		contentLength: Long,
		content: Source,
	) : this(
		status = status,
		mimeType = mimeType,
		charset = charset,
		headers = emptyMap(),
		contentLength = contentLength,
		content,
	)

	actual constructor(
		mimeType: String?,
		charset: String?,
		contentLength: Long,
		content: Source,
	) : this(
		status = 200,
		mimeType = mimeType,
		charset = charset,
		contentLength = contentLength,
		content,
	)

	actual companion object
}
