package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.WebRequest
import kokoro.app.ui.engine.web.WebResource
import kotlin.jvm.JvmField

data class UiHtml(
	@JvmField val spec: UiSpec,
	@JvmField val templ: UiTempl,
) : WebResource {

	constructor() : this(BlankUiTempl)
	constructor(templ: UiTempl) : this(UiSpec(), templ)

	// --

	override suspend fun apply(request: WebRequest) = templ.buildHtmlResponse(spec)
}
