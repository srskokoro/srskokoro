package kokoro.app.ui.engine

import app.cash.redwood.widget.Widget
import kokoro.app.ui.engine.widget.WvWidget
import kokoro.app.ui.engine.widget.WvWidgetChildren

// TODO! Stub
class WvBinder(
	unitIdMapper: WvUnitIdMapper,
	jsEngine: WvJsEngine?,
) {
	val root = WvWidget()

	val rootChildren: WvWidgetChildren = object : WvWidgetChildren() {
		override fun insert(index: Int, widget: Widget<WvWidget>): Unit = TODO("Not yet implemented")
		override fun move(fromIndex: Int, toIndex: Int, count: Int): Unit = TODO("Not yet implemented")
		override fun onModifierUpdated(): Unit = TODO("Not yet implemented")
		override fun remove(index: Int, count: Int): Unit = TODO("Not yet implemented")
	}

	fun onConcludeChanges() {}
}
