package kokoro.app.ui.wv.widget

import kokoro.app.ui.wv.UpdatesBuilder
import kokoro.app.ui.wv.WvBinder

class WvRoot internal constructor(binder: WvBinder) : WvWidget(binder) {

	override fun UpdatesBuilder.onBindUpdates() = Unit
}
