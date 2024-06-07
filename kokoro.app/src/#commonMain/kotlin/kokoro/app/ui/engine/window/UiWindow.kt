package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.Ui
import kokoro.app.ui.engine.web.WebUriRouting
import kokoro.app.ui.engine.web.WvLibAssetResolver
import kokoro.app.ui.engine.web.plus
import kotlin.jvm.JvmField

abstract class UiWindow(
	@JvmField val ui: Ui,
	@JvmField val url: String,
	context: WvContext,
) : WvWindow(context) {

	constructor(ui: Ui, context: WvContext) : this(ui, ui.url, context)

	init {
		context.title = ui.title
		context.load(url)
	}

	override suspend fun initWebUriResolver() = WebUriRouting {
		route(url, ui)
	} + WvLibAssetResolver.PRESET
}
