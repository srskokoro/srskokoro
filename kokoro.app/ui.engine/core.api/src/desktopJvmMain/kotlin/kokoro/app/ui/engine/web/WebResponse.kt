package kokoro.app.ui.engine.web

import okio.Source

actual class WebResponse actual constructor(
	actual val status: Int,
	actual val mimeType: String?,
	actual val encoding: String?,
	actual val headers: Map<String, String>,
	actual val dataLength: Long,
	actual val data: Source,
) {
	init {
		WebResponse_checkStatus_nonAndroid(status)
	}

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		dataLength: Long,
		data: Source,
	) : this(
		status = status,
		mimeType = mimeType,
		encoding = encoding,
		headers = emptyMap(),
		dataLength = dataLength,
		data,
	)

	actual constructor(
		mimeType: String?,
		encoding: String?,
		dataLength: Long,
		data: Source,
	) : this(
		status = 200,
		mimeType = mimeType,
		encoding = encoding,
		dataLength = dataLength,
		data,
	)

	actual companion object
}
