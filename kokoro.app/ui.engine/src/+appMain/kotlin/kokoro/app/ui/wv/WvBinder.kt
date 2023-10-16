package kokoro.app.ui.wv

import app.cash.redwood.Modifier
import assertUnreachable
import kokoro.app.ui.wv.modifier.GlobalModifier
import kokoro.app.ui.wv.modifier.ModifierDelegate
import kokoro.app.ui.wv.widget.WvWidget
import kokoro.internal.collections.MapComputeFunction
import kokoro.internal.collections.computeIfAbsent
import korlibs.datastructure.FastIntMap
import korlibs.datastructure.IntDeque
import kotlin.jvm.JvmField

class WvBinder(
	@JvmField val tIdMapper: TemplateIdMapper,
) {
	@JvmField internal val bindingCommand = StringBuilder()
	private var bindingCommand_lengthBackup: Int = 0

	private val modifierBindingAction = ModifierBindingAction(this)

	private var deferredException: Throwable? = null

	fun deferException(ex: Throwable) {
		val prev = deferredException
		if (prev == null) {
			deferredException = ex
		} else {
			prev.addSuppressed(ex)
		}
	}

	//#region ID Binding

	private val widgetIdPool = IntDeque()
	private var widgetIdLastGen = WIDGET_ID_ROOT

	private val callbackIdPool = IntDeque()
	private var callbackIdLastGen = -1

	@Suppress("NOTHING_TO_INLINE")
	internal inline fun obtainWidgetId_inline(): Int {
		val idPool = widgetIdPool
		if (idPool.isEmpty()) {
			val newId = widgetIdLastGen + WIDGET_ID_INC
			if (newId <= WIDGET_ID_ROOT) throw E_IdGenOverflow()
			widgetIdLastGen = newId
			return newId
		}
		return idPool.removeFirst()
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun recycleWidgetId(id: Int): Unit =
		widgetIdPool.addLast(id)

	@Suppress("NOTHING_TO_INLINE")
	private inline fun obtainCallbackId_inline(): Int {
		val idPool = callbackIdPool
		if (idPool.isEmpty()) {
			val newId = callbackIdLastGen + 1
			if (newId < 0) throw E_IdGenOverflow()
			callbackIdLastGen = newId
			return newId
		}
		return idPool.removeFirst()
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun recycleCallbackId(id: Int): Unit =
		callbackIdPool.addLast(id)

	//#endregion

	//#region Widget Binding

	@JvmField internal val widgetStatusChanges = ArrayList<WvWidget>()

	private val layoutStack = ArrayList<WvWidget>()

	private fun layoutStackPush(widget: WvWidget) {
		val status = widget._widgetStatus
		if (status and WS_TRACKED_IN_LAYOUT_STACK == 0) {
			widget._widgetStatus = status or WS_TRACKED_IN_LAYOUT_STACK

			// NOTE: We're pushing the ancestors first before the widget, so
			// that later on, we may process the list in reverse, and the
			// ancestor of any widget in the list is guaranteed to be processed
			// last.

			widget._parent?.let {
				layoutStackPush(it.parent)
			}

			layoutStack.add(widget)
		}
	}

	var onConcludeChangesError: (ex: Throwable) -> Unit = { ex -> throw ex }

	fun onConcludeChanges() {
		val cmd = bindingCommand
		bindingCommand_lengthBackup = cmd.length

		try {
			val modifierBindingAction = modifierBindingAction
			val statusChanges = widgetStatusChanges
			for (i in statusChanges.indices) {
				val widget = statusChanges[i]
				val status = widget._widgetStatus

				if (status and WS_GARBAGE != 0) {
					val widgetId = widget._widgetId
					if (widgetId <= WIDGET_ID_ROOT) {
						if (widgetId != WIDGET_ID_ROOT) {
							assertUnreachable { "Widget already unbound" }
						} else {
							widget._widgetStatus = 0 // Consume
							error("Should never unbind root")
						}
						continue
					}
					widget._widgetId = -1

					cmd.append("D$(")
					cmd.append(widgetId)
					cmd.appendLine(')')

					// NOTE: The fact that we recycle the widget IDs right away,
					// without even waiting for the "unbind" command to be
					// evaluated by the `WebView`, is the reason we shouldn't
					// expose those widget IDs to the API clients, i.e., the
					// widget updaters, the modifier binders, etc., so that they
					// may not accidentally manipulate a stale ID.
					recycleWidgetId(widgetId)
					continue // Already unbound. Nothing to do.
				}

				if (status and WS_UPDATE != 0) {
					cmd.append("U$(")
					cmd.append(widget._widgetId)

					if (status and WS_MODEL_UPDATE != 0) {
						widget.bindUpdates(UpdatesBuilder(cmd))
					}
					if (status and WS_MODIFIER_UPDATE != 0) {
						modifierBindingAction.on(widget)
					}

					cmd.appendLine(')')
				}

				if (status and WS_TREE_REQUESTED_LAYOUT != 0) {
					layoutStackPush(widget)
					// NOTE: Widget status will be consumed later instead.
				} else {
					widget._widgetStatus = 0 // Consume
				}
			}
			statusChanges.clear()
			modifierBindingAction.clear() // To allow GC

			val layoutStack = layoutStack
			for (i in layoutStack.indices.reversed()) {
				val widget = layoutStack[i]

				val status = widget._widgetStatus
				widget._widgetStatus = 0 // Consume

				if (status and (WS_REQUESTED_LAYOUT or WS_GARBAGE) == WS_REQUESTED_LAYOUT) {
					cmd.append("L$(")
					cmd.append(widget._widgetId)
					cmd.appendLine(')')
				}
			}
			layoutStack.clear()

			// --
		} catch (ex: Throwable) {
			deferException(ex)
		}

		if (deferredException == null) {
			bindingCommand_lengthBackup = 0
			executeBindingCommand()
			return // Early exit
		}

		// --
		// Error handling

		onConcludeChanges_fail()
	}

	private fun onConcludeChanges_fail() {
		val thrown = deferredException
			?: AssertionError("Required value was null.")

		deferredException = null // Consume

		bindingCommand_lengthBackup.let {
			bindingCommand_lengthBackup = 0
			bindingCommand.setLength(it)
		}

		modifierBindingAction.clear() // To allow GC

		widgetStatusChanges.let { widgets ->
			for (widget in widgets) widget._widgetStatus = 0 // Consume
			widgets.clear()
		}
		layoutStack.let { widgets ->
			for (widget in widgets) widget._widgetStatus = 0 // Consume
			widgets.clear()
		}

		try {
			executeBindingCommand()
		} catch (ex: Throwable) {
			thrown.addSuppressed(ex)
		}

		onConcludeChangesError.invoke(thrown)
	}

	private fun executeBindingCommand() {
		// TODO Send binding command to `WebView`
	}

	private class ModifierBindingAction(binder: WvBinder) : (Modifier.Element) -> Unit {
		private val out = binder.bindingCommand
		private val tIdMapper = binder.tIdMapper

		private var widget: WvWidget? = null
		private var oldModifierMap: LinkedHashMap<Int, Modifier.Element>? = null

		fun clear() {
			widget = null
			oldModifierMap?.clear()
		}

		fun on(widget: WvWidget) {
			try {
				this.widget = widget

				val oldModifierMap = widget._modifierMap
				this.oldModifierMap = oldModifierMap

				val newModifierMap = LinkedHashMap<Int, Modifier.Element>()
				widget._modifierMap = newModifierMap

				out.append(','); out.append('[')
				widget._modifier.forEach(this)
				conclude(out, ']')

				if (newModifierMap.isEmpty()) widget._modifierMap = null

				oldModifierMap?.forEach { (mId, _) ->
					out.append(','); out.append(mId)
				}
			} catch (ex: Throwable) {
				widget._modifierMap = null
				throw ex
			}
		}

		override fun invoke(modifier: Modifier.Element) {
			val delegate: ModifierDelegate<*> =
				if (modifier is GlobalModifier) modifier
				else widget?._parent?.onBindScopedModifier(modifier)
					?: return // Skip

			val mId = tIdMapper.invoke(delegate.templKey)
			val prev = oldModifierMap?.remove(mId)
			if (prev == null || prev != modifier) {
				out.append(mId); out.append(',')
				out.append('[')
				delegate.bind(ArgumentsBuilder(out), modifier)
				out.append(']'); out.append(',')
			}
			widget?._modifierMap?.put(mId, modifier)
		}
	}

	//#endregion

	//#region Callback Binding

	private val boundCallbackIds = HashMap<CallbackRouter, Int>()
	private val boundCallbacks = FastIntMap<CallbackRouter>()

	private val callbackBinder = MapComputeFunction<CallbackRouter, Int> { callback ->
		obtainCallbackId_inline().also { newId ->
			boundCallbacks[newId] = callback
		}
	}

	fun onCallbackEvent(callbackId: Int, data: String) {
		boundCallbacks[callbackId]?.route(data)
	}

	fun forceUnbindCallback(callbackId: Int) {
		val callback = boundCallbacks.set(callbackId, null)
		if (callback != null) {
			boundCallbackIds.remove(callback)
			recycleCallbackId(callbackId)
			return
		}
		throw IllegalArgumentException("Callback already unbound for ID: $callbackId")
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun forceUnbindCallback(callbackId: CallbackId) =
		forceUnbindCallback(callbackId.id)

	fun callback(callback: CallbackRouter): CallbackId {
		return CallbackId(boundCallbackIds.computeIfAbsent(callback, callbackBinder))
	}

	inline fun <T1> callback(
		s1: T1, crossinline route: (
			data: String,
			s1: T1,
		) -> Unit
	) = callback(CallbackRouter(s1, route))

	inline fun <T1, T2> callback(
		s1: T1, s2: T2, crossinline route: (
			data: String,
			s1: T1, s2: T2,
		) -> Unit
	) = callback(CallbackRouter(s1, s2, route))

	inline fun <T1, T2, T3> callback(
		s1: T1, s2: T2, s3: T3, crossinline route: (
			data: String,
			s1: T1, s2: T2, s3: T3,
		) -> Unit
	) = callback(CallbackRouter(s1, s2, s3, route))

	//#endregion
}

private fun E_IdGenOverflow() = Error("Overflow: ID generation already exhausted")
