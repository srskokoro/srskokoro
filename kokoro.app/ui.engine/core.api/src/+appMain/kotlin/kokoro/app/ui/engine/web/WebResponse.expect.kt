package kokoro.app.ui.engine.web

import kokoro.internal.RELEASE
import okio.Source

expect class WebResponse {
	val status: Int
	val mimeType: String?
	val encoding: String?
	val headers: Map<String, String>
	val contentLength: Long
	val content: Source

	constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		headers: Map<String, String>,
		contentLength: Long,
		content: Source,
	)

	constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		contentLength: Long,
		content: Source,
	)

	constructor(
		mimeType: String?,
		encoding: String?,
		contentLength: Long,
		content: Source,
	)

	companion object
}

internal fun WebResponse_checkStatus_nonAndroid(status: Int) {
	if (RELEASE) return // Skip check on release builds!

	// The following check is to ensure that `status` is consistent with the
	// expected behavior on Android. See, `android.webkit.WebResourceResponse.setStatusCodeAndReasonPhrase()`
	if (status < 100) throw IllegalArgumentException("status code can't be less than 100.")
	if (status > 599) throw IllegalArgumentException("status code can't be greater than 599.")
	if (status in 300..399) throw IllegalArgumentException("status code can't be in the [300, 399] range.")
}