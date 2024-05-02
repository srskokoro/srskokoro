package kokoro.app.ui.engine.web

import kokoro.internal.DEBUG
import okio.Source
import kotlin.jvm.JvmField

class WebResponse {
	@JvmField val status: Int
	@JvmField val mimeType: String?
	@JvmField val charset: String?
	@JvmField val headers: Map<String, String>
	@JvmField val contentLength: Long
	@JvmField val content: Source

	constructor(
		status: Int,
		mimeType: String?,
		charset: String?,
		headers: Map<String, String>,
		contentLength: Long,
		content: Source,
	) {
		if (DEBUG) {
			// The following check ensures that `status` is consistent with the
			// expected behavior on Android. See, `android.webkit.WebResourceResponse.setStatusCodeAndReasonPhrase()`
			if (status < 100) throw IllegalArgumentException("status code can't be less than 100.")
			if (status > 599) throw IllegalArgumentException("status code can't be greater than 599.")
			if (status in 300..399) throw IllegalArgumentException("status code can't be in the [300, 399] range.")
		}
		this.status = status
		this.mimeType = mimeType
		this.charset = charset
		this.headers = headers
		this.contentLength = contentLength
		this.content = content
	}

	constructor(
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

	constructor(
		mimeType: String?,
		charset: String?,
		contentLength: Long,
		content: Source,
	) : this(
		status = 200,
		mimeType = mimeType,
		charset = charset,
		headers = emptyMap(),
		contentLength = contentLength,
		content,
	)
}
