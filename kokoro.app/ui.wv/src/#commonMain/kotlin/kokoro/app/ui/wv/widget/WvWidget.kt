package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.ArgumentsBuilder
import kokoro.app.ui.wv.WS_GARBAGE
import kokoro.app.ui.wv.WS_MODIFIER_UPDATE
import kokoro.app.ui.wv.WS_REQUESTED_LAYOUT
import kokoro.app.ui.wv.WS_TRACKED
import kokoro.app.ui.wv.WS_UPDATE
import kokoro.app.ui.wv.WvBinder
import kokoro.app.ui.wv.conclude
import kotlin.jvm.JvmField

abstract class WvWidget(templateId: Int, @JvmField val binder: WvBinder) : Widget<WvWidget> {
	@PublishedApi @JvmField internal var _widgetId: Int
	@PublishedApi @JvmField internal var _widgetStatus: Int

	/**
	 * NOTE: This field should be interpreted as holding the last known parent
	 * of this widget. The widget may not necessarily be the parent's child
	 * anymore, but should the widget be parented to another, this field must
	 * reflect that new state.
	 */
	@JvmField internal var parent: WvWidgetChildren? = null

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
		cmd.appendLine(')')
	}

	internal inline fun flagStatus(flags: Int) {
		val oldStatus = _widgetStatus
		if (oldStatus and WS_TRACKED == 0) {
			binder.widgetStatusChanges.add(this)
		}
		_widgetStatus = oldStatus or (flags or WS_TRACKED)
	}

	fun postUpdate() {
		// NOTE: Modifier updates must be rebound on top of widget model
		// updates, even if there are only widget model updates.
		flagStatus(WS_UPDATE)
	}

	fun postModifierUpdate() {
		flagStatus(WS_MODIFIER_UPDATE)
	}

	fun requestLayout() {
		flagStatus(WS_REQUESTED_LAYOUT)
	}

	internal inline fun bindUpdates(cmd: StringBuilder) {
		cmd.append('[')
		val args = ArgumentsBuilder(cmd)
		args.onBindUpdates()
		args.conclude(']')
	}

	abstract fun ArgumentsBuilder.onBindUpdates()

	// --

	@Suppress("OVERRIDE_BY_INLINE")
	final override val value: WvWidget
		inline get() = this

	@JvmField internal var _modifier: Modifier = Modifier
	final override var modifier: Modifier
		get() = _modifier
		set(v) {
			postModifierUpdate()
			_modifier = v
		}
}
