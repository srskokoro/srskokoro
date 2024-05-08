package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.WebResponse

fun interface UiTempl {

	suspend fun buildHtmlResponse(spec: UiSpec): WebResponse
}
