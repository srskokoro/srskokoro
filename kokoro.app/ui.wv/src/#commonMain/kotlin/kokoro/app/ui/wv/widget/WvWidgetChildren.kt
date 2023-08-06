package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.modifier.ModifierBinder
import kotlin.jvm.JvmField

open class WvWidgetChildren(@JvmField val parent: WvWidget) : Widget.Children<WvWidget> {

	override fun insert(index: Int, widget: Widget<WvWidget>) {
		val parent = parent
		val cmd = parent.binder.bindingCommand

		cmd.append("I$(")
		cmd.append(parent._widgetId)

		cmd.append(','); cmd.append(index)

		cmd.append(',')
		val child = widget as WvWidget
		child.parent = this
		cmd.append(child._widgetId)

		cmd.appendLine(')')
	}

	override fun move(fromIndex: Int, toIndex: Int, count: Int) {
		val parent = parent
		val cmd = parent.binder.bindingCommand

		cmd.append("S$(")
		cmd.append(parent._widgetId)

		cmd.append(','); cmd.append(fromIndex)
		cmd.append(','); cmd.append(toIndex)
		cmd.append(','); cmd.append(count)

		cmd.appendLine(')')
	}

	override fun remove(index: Int, count: Int) {
		val parent = parent
		val cmd = parent.binder.bindingCommand

		cmd.append("R$(")
		cmd.append(parent._widgetId)

		cmd.append(','); cmd.append(index)
		cmd.append(','); cmd.append(count)

		cmd.appendLine(')')
	}

	override fun onModifierUpdated() = Unit

	open fun ModifierBinder.onBindScopedModifier(modifier: Modifier) = Unit
}
