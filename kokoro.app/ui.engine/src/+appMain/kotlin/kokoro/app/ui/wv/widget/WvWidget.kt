package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.UpdatesBuilder
import kokoro.app.ui.wv.WS_EXT_SHL
import kokoro.app.ui.wv.WS_GARBAGE
import kokoro.app.ui.wv.WS_CORE_MASK
import kokoro.app.ui.wv.WS_MODEL_UPDATE
import kokoro.app.ui.wv.WS_MODIFIER_UPDATE
import kokoro.app.ui.wv.WS_REQUESTED_LAYOUT
import kokoro.app.ui.wv.WS_TRACKED
import kokoro.app.ui.wv.WvBinder
import kotlin.jvm.JvmField

abstract class WvWidget(templId: Int, @JvmField val binder: WvBinder) : Widget<WvWidget> {
	constructor(templKey: String, binder: WvBinder) : this(binder.tIdMapper.invoke(templKey), binder)

	@PublishedApi @JvmField internal var _widgetId: Int
	@PublishedApi @JvmField internal var _widgetStatus: Int

	/**
	 * NOTE: This field should be interpreted as holding the last known parent
	 * of this widget. The widget may not necessarily be the parent's child
	 * anymore, but should the widget be parented to another, this field must
	 * reflect that new state.
	 */
	@PublishedApi @JvmField internal var _parent: WvWidgetChildren? = null
	inline val parent get() = _parent

	init {
		val binder = binder
		val widgetId = binder.obtainWidgetId_inline()

		_widgetId = widgetId
		_widgetStatus = WS_GARBAGE
		binder.widgetStatusChanges.add(@Suppress("LeakingThis") this)

		val cmd = binder.bindingCommand
		cmd.append("C$(")
		cmd.append(templId)
		cmd.append(',')
		cmd.append(widgetId)
		cmd.appendLine(')')
	}

	@Suppress("NOTHING_TO_INLINE")
	internal inline fun flagStatus(flags: Int) {
		val oldStatus = _widgetStatus
		if (oldStatus and WS_TRACKED == 0) {
			binder.widgetStatusChanges.add(this)
		}
		_widgetStatus = oldStatus or (flags or WS_TRACKED)
	}


	fun postUpdate() {
		flagStatus(WS_MODEL_UPDATE)
	}

	fun postUpdate(extFlags: Int) {
		flagStatus(WS_MODEL_UPDATE or (extFlags shl WS_EXT_SHL))
	}

	fun meldExtFlags(extFlags: Int) {
		_widgetStatus = _widgetStatus or (extFlags shl WS_EXT_SHL)
	}

	fun setExtFlags(extFlags: Int) {
		_widgetStatus = (_widgetStatus and WS_CORE_MASK) or (extFlags shl WS_EXT_SHL)
	}

	fun getExtFlags() = _widgetStatus ushr WS_EXT_SHL


	fun postModifierUpdate() {
		flagStatus(WS_MODIFIER_UPDATE)
	}

	fun requestLayout() {
		flagStatus(WS_REQUESTED_LAYOUT)
	}


	@Suppress("NOTHING_TO_INLINE")
	internal inline fun bindUpdates(updater: UpdatesBuilder) {
		updater.onBindUpdates()
	}

	abstract fun UpdatesBuilder.onBindUpdates()

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

	@JvmField internal var _modifierMap: LinkedHashMap<Int, Modifier.Element>? = null
}
