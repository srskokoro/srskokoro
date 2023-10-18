package kokoro.app.ui.wv.widget

import kokoro.app.ui.wv.UpdatesBuilder
import kokoro.app.ui.wv.WvBinder

class WvRootWidget internal constructor(binder: WvBinder) : WvWidget(binder) {

	override fun UpdatesBuilder.onBindUpdates() = Unit
}
