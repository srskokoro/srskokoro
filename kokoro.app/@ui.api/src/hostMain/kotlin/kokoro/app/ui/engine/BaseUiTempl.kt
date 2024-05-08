package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.WebResponse
import kokoro.internal.io.asAppendableUtf8
import kokoro.internal.io.asClearing
import okio.Buffer

abstract class BaseUiTempl : UiTempl {

	protected abstract suspend fun buildHtmlContent(spec: UiSpec, out: Appendable)

	override suspend fun buildHtmlResponse(spec: UiSpec): WebResponse {
		val buffer = Buffer()
		buildHtmlContent(spec, buffer.asAppendableUtf8())
		return WebResponse(
			mimeType = "text/html",
			charset = "utf-8",
			buffer.size, buffer.asClearing(),
		)
	}
}
