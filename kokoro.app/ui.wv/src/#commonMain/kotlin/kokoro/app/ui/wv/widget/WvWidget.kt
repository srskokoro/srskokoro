package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.ArgumentsBuilder
import kokoro.app.ui.wv.WS_GARBAGE
import kokoro.app.ui.wv.WS_MODIFIER_UPDATE
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

	internal inline fun postStatus(mutation: (oldStatus: Int) -> Int) {
		var status = mutation(_widgetStatus)
		if (status and WS_TRACKED == 0) {
			status = status or WS_TRACKED
			binder.widgetStatusChanges.add(this)
		}
		_widgetStatus = status
	}

	fun postUpdate() {
		// NOTE: Modifier updates must be rebound on top of widget model
		// updates, even if there are only widget model updates.
		postStatus { it or WS_UPDATE }
	}

	fun postModifierUpdate() {
		postStatus { it or WS_MODIFIER_UPDATE }
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
