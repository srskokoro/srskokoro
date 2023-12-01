package kokoro.app.ui.engine

import kokoro.app.ui.engine.widget.WvWidget
import kokoro.app.ui.engine.widget.WvWidgetChildren

// TODO! Stub
abstract class WvBinder {

	abstract val root: WvWidget
	abstract val rootChildren: WvWidgetChildren

	abstract fun onConcludeChanges()

	companion object {
		operator fun invoke(
			unitIdMapper: WvUnitIdMapper,
			jsEngine: WvJsEngine,
		): WvBinder = TODO("Not yet implemented")
	}
}
