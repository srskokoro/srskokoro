package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.Ui
import kokoro.app.ui.engine.web.HtmlTitleSpec
import kokoro.app.ui.engine.web.WebUriRouting
import kokoro.app.ui.engine.web.WvLibAssetResolver
import kokoro.app.ui.engine.web.plus
import kotlin.jvm.JvmField

abstract class UiWindow(
	@JvmField val ui: Ui,
	context: WvContext,
) : WvWindow(context) {

	init {
		ui.spec.query(HtmlTitleSpec.PROP_TITLE)?.let {
			context.title = it
		}
		context.load(ui.url)
	}

	override suspend fun initWebUriResolver() = WebUriRouting {
		route(ui.url, ui.html)
	} + WvLibAssetResolver.PRESET
}
