package kokoro.app.ui.engine.web

import okio.Source

actual class WebResponse actual constructor(
	actual val status: Int,
	actual val mimeType: String?,
	actual val encoding: String?,
	actual val headers: Map<String, String>,
	actual val data: Source,
) {
	init {
		common_checkStatus(status)
	}

	actual constructor(
		status: Int,
		mimeType: String?,
		encoding: String?,
		data: Source,
	) : this(
		status = status,
		mimeType = mimeType,
		encoding = encoding,
		headers = emptyMap(),
		data = data,
	)

	actual constructor(
		mimeType: String?,
		encoding: String?,
		data: Source,
	) : this(
		status = 200,
		mimeType = mimeType,
		encoding = encoding,
		data = data,
	)
}
