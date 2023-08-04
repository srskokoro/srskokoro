package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.ArgumentsBuilder
import kokoro.app.ui.wv.WS_GARBAGE
import kokoro.app.ui.wv.WS_UPDATE
import kokoro.app.ui.wv.WvBinder
import kotlin.jvm.JvmField

abstract class WvWidget(templateId: Int, private val binder: WvBinder) : Widget<WvWidget> {
	@JvmField internal var _widgetId: Int
	@JvmField internal var _widgetStatus: Int

	init {
		val binder = binder
		val widgetId = binder.obtainWidgetId_inline()

		_widgetId = widgetId
		_widgetStatus = WS_GARBAGE
		binder.widgetStatusChanges.add(@Suppress("LeakingThis") this)

		val cmd = binder.bindingCommand
		cmd.append("C$(")
		cmd.append(templateId)
		cmd.append(',')
		cmd.append(widgetId)
		cmd.append(")\n")
	}

	protected fun postUpdate() {
		_widgetStatus = _widgetStatus or WS_UPDATE
		binder.widgetStatusChanges.add(this)
	}

	internal fun bindUpdates(cmd: StringBuilder) {
		val args = ArgumentsBuilder(cmd)
		args.onBindUpdates()
		args.conclude()
	}

	protected abstract fun ArgumentsBuilder.onBindUpdates()

	// --

	@Suppress("OVERRIDE_BY_INLINE")
	final override val value: WvWidget
		inline get() = this

	final override var modifier: Modifier = Modifier
}
