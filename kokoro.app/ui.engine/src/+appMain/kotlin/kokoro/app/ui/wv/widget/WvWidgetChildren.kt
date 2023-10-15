package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.WS_GARBAGE
import kokoro.app.ui.wv.WS_GARBAGE_INV
import kokoro.app.ui.wv.modifier.ModifierDelegate
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

		child._parent = this
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
			child.flagStatus(WS_GARBAGE)
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

	open fun onBindScopedModifier(modifier: Modifier.Element): ModifierDelegate<*>? = null

	@Suppress("NOTHING_TO_INLINE")
	inline fun requestLayout() = parent.requestLayout()

	// --

	abstract class WithScopedModifier(parent: WvWidget) : WvWidgetChildren(parent) {

		override fun insert(index: Int, widget: Widget<WvWidget>) {
			(widget as WvWidget).postModifierUpdate()
			super.insert(index, widget)
			requestLayout()
		}

		override fun move(fromIndex: Int, toIndex: Int, count: Int) {
			super.move(fromIndex, toIndex, count)
			requestLayout()
		}

		override fun remove(index: Int, count: Int) {
			super.remove(index, count)
			requestLayout()
		}

		override fun onModifierUpdated() {
			requestLayout()
		}

		abstract override fun onBindScopedModifier(modifier: Modifier.Element): ModifierDelegate<*>?

		companion object {
			inline operator fun invoke(
				parent: WvWidget, crossinline onBindScopedModifier: (modifier: Modifier.Element) -> ModifierDelegate<*>?,
			) = object : WithScopedModifier(parent) {
				override fun onBindScopedModifier(modifier: Modifier.Element) = onBindScopedModifier(modifier)
			}
		}
	}
}
