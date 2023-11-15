package kokoro.app.ui.engine.web

import okio.Source

expect class WebResponse {
	val status: Int
	val mimeType: String?
	val encoding: String?
	val headers: Map<String, String>
	val data: Source

	constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		headers: Map<String, String>,
		data: Source,
	)

	constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		data: Source,
	)

	constructor(
		mimeType: String?,
		encoding: String?,
		data: Source,
	)
}

internal fun WebResponse.common_checkStatus(status: Int) {
	// The following check is to ensure that `status` is consistent with the
	// expected behavior on Android. See, `android.webkit.WebResourceResponse.setStatusCodeAndReasonPhrase()`
	if (status < 100) throw IllegalArgumentException("status code can't be less than 100.")
	if (status > 599) throw IllegalArgumentException("status code can't be greater than 599.")
	if (status in 300..399) throw IllegalArgumentException("status code can't be in the [300, 399] range.")
}
