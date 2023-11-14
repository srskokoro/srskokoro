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
