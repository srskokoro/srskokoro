package kokoro.app.ui.engine

import app.cash.redwood.widget.Widget
import kokoro.app.ui.engine.web.WebContextResolver
import kokoro.app.ui.engine.widget.WvWidget

interface WvSetup {
	val initUrl: String
	val contexts: WebContextResolver
	fun onNewComposition(binder: WvBinder): Widget.Provider<WvWidget>
}
