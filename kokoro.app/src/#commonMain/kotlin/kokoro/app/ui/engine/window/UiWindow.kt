package kokoro.app.ui.engine.window

import androidx.annotation.CallSuper
import kokoro.app.ui.engine.Ui
import kokoro.app.ui.engine.web.HTTPX_UI_X_FAVICON
import kokoro.app.ui.engine.web.HTTPX_UI_X_PLATFORM_JS
import kokoro.app.ui.engine.web.WebResponse
import kokoro.app.ui.engine.web.WebUriResolver
import kokoro.app.ui.engine.web.WebUriRouting
import kokoro.app.ui.engine.web.WvLibAssetsResolver
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

	override suspend fun initWebUriResolver(): WebUriResolver =
		WebUriRouting { initWebUriRouting(this) } + WvLibAssetsResolver.PRESET

	@CallSuper
	protected open suspend fun initWebUriRouting(routes: WebUriRouting.Builder) {
		routes.route(url, ui)
		routes.route(HTTPX_UI_X_PLATFORM_JS, context.initPlatformJs())
		routes.route(HTTPX_UI_X_FAVICON) { WebResponse(mimeType = "image/png") }
	}
}
