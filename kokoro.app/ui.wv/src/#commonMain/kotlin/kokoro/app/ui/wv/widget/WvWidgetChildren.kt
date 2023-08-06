package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.WS_GARBAGE
import kokoro.app.ui.wv.WS_GARBAGE_INV
import kokoro.app.ui.wv.modifier.ModifierBinder
import kokoro.internal.collections.move
import kokoro.internal.collections.remove
import kotlin.jvm.JvmField

open class WvWidgetChildren(@JvmField val parent: WvWidget) : Widget.Children<WvWidget> {

	@PublishedApi @JvmField internal val _widgets = ArrayList<WvWidget>()
	inline val widgets: List<WvWidget> get() = _widgets

	override fun insert(index: Int, widget: Widget<WvWidget>) {
		val child = widget as WvWidget

		// Unflag widget as "garbage" (if flagged before)
		child._widgetStatus = child._widgetStatus and WS_GARBAGE_INV

		child.parent = this
		_widgets.add(index, child)

		val parent = parent
		val cmd = parent.binder.bindingCommand

		cmd.append("I$(")
		cmd.append(parent._widgetId)

		cmd.append(','); cmd.append(index)
		cmd.append(','); cmd.append(child._widgetId)

		cmd.appendLine(')')
	}

	override fun move(fromIndex: Int, toIndex: Int, count: Int) {
		_widgets.move(fromIndex, toIndex, count)

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
		_widgets.remove(index, count) { child ->
			// Flag widget as potential "garbage"
			child.postStatus { it or WS_GARBAGE }
		}

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
