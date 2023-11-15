package kokoro.app.ui.engine.web

import kokoro.internal.RELEASE
import kokoro.internal.io.nullSource
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

	companion object
}

inline val WebResponse.isSwitchingWebContext get() = this === WebResponse.forSwitchingWebContext()

fun WebResponse.Companion.forSwitchingWebContext(): WebResponse = _forSwitchingWebContext
private val _forSwitchingWebContext = WebResponse(418 /* I'm a teapot */, null, null, nullSource())

internal fun WebResponse_checkStatus_nonAndroid(status: Int) {
	if (RELEASE) return // Skip check on release builds!

	// The following check is to ensure that `status` is consistent with the
	// expected behavior on Android. See, `android.webkit.WebResourceResponse.setStatusCodeAndReasonPhrase()`
	if (status < 100) throw IllegalArgumentException("status code can't be less than 100.")
	if (status > 599) throw IllegalArgumentException("status code can't be greater than 599.")
	if (status in 300..399) throw IllegalArgumentException("status code can't be in the [300, 399] range.")
}
