package kokoro.app.ui.wv

import app.cash.redwood.Modifier
import assertUnreachable
import kokoro.app.ui.wv.modifier.GlobalModifier
import kokoro.app.ui.wv.modifier.ModifierBinder
import kokoro.app.ui.wv.widget.WvWidget
import kokoro.app.ui.wv.widget.WvWidgetChildren
import kokoro.internal.collections.MapComputeFunction
import kokoro.internal.collections.computeIfAbsent
import korlibs.datastructure.FastIntMap
import korlibs.datastructure.IntDeque
import kotlin.jvm.JvmField

class WvBinder {
	@JvmField internal val bindingCommand = StringBuilder()
	private var bindingCommand_lengthBackup: Int = 0

	private val modifierBindingAction = ModifierBindingAction(ModifierBinder(bindingCommand))

	private var deferredException: Throwable? = null

	fun deferException(ex: Throwable) {
		val prev = deferredException
		if (prev == null) {
			deferredException = ex
		} else {
			prev.addSuppressed(ex)
		}
	}

	//region ID Binding

	private val widgetIdPool = IntDeque()
	private var widgetIdLastGen = 0

	private val callbackIdPool = IntDeque()
	private var callbackIdLastGen = 0

	internal fun obtainWidgetId_inline(): Int {
		val idPool = widgetIdPool
		if (idPool.isEmpty()) {
			val newId = widgetIdLastGen + WIDGET_ID_INC
			if (newId <= 0) throw E_IdGenOverflow()
			widgetIdLastGen = newId
			return newId
		}
		return idPool.removeFirst()
	}

	private inline fun recycleWidgetId(id: Int): Unit =
		widgetIdPool.addLast(id)

	private fun obtainCallbackId_inline(): Int {
		val idPool = callbackIdPool
		if (idPool.isEmpty()) {
			val newId = callbackIdLastGen + 1
			if (newId <= 0) throw E_IdGenOverflow()
			callbackIdLastGen = newId
			return newId
		}
		return idPool.removeFirst()
	}

	private inline fun recycleCallbackId(id: Int): Unit =
		callbackIdPool.addLast(id)

	//endregion

	//region Widget Binding

	@JvmField internal val widgetStatusChanges = ArrayList<WvWidget>()

	var onConcludeChangesError: (ex: Throwable) -> Unit = { ex -> throw ex }

	fun concludeChanges() {
		val cmd = bindingCommand
		bindingCommand_lengthBackup = cmd.length

		val statusChanges = widgetStatusChanges
		val modifierBindingAction = modifierBindingAction

		try {
			for (i in statusChanges.indices) {
				val widget = statusChanges[i]

				val status = widget._widgetStatus
				widget._widgetStatus = 0 // Consume

				if (status and WS_GARBAGE != 0) {
					val widgetId = widget._widgetId
					widget._widgetId = 0

					if (widgetId == 0) {
						assertUnreachable { "Widget already unbound" }
						continue
					}

					cmd.append("D$(")
					cmd.append(widgetId)
					cmd.appendLine(')')

					// NOTE: The fact that we recycle the widget IDs right away,
					// without even waiting for the "unbind" command to be evaluated
					// by the `WebView`, is the reason we shouldn't expose those
					// widget IDs to the API clients, i.e., the widget updaters, the
					// modifier binders, etc., so that they may not accidentally
					// manipulate a stale ID.
					recycleWidgetId(widgetId)
					continue // Already unbound. Nothing to do.
				}

				if (status and WS_UPDATE != 0) {
					cmd.append("U$(")
					cmd.append(widget._widgetId)

					cmd.append(',')
					if (status and WS_MODEL_UPDATE != 0) {
						widget.bindUpdates(cmd)
					} else {
						cmd.append("null")
					}

					modifierBindingAction.parent = widget.parent
					widget._modifier.forEach(modifierBindingAction)

					cmd.appendLine(')')
				}
			}
		} catch (ex: Throwable) {
			deferException(ex)
		}

		modifierBindingAction.parent = null // To allow GC

		// Done. Everything already processed.
		statusChanges.clear()

		if (deferredException == null) {
			bindingCommand_lengthBackup = 0
			executeBindingCommand()
			return // Early exit
		}

		// --
		// Error handling

		concludeChanges_fail()
	}

	private fun concludeChanges_fail() {
		val thrown = deferredException
			?: AssertionError("Required value was null.")

		bindingCommand_lengthBackup.let {
			bindingCommand_lengthBackup = 0
			bindingCommand.setLength(it)
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

	private class ModifierBindingAction(
		private val binder: ModifierBinder,
	) : (Modifier) -> Unit {
		@JvmField var parent: WvWidgetChildren? = null

		override fun invoke(modifier: Modifier) {
			if (modifier is GlobalModifier) with(modifier) {
				binder.onBind()
			} else parent?.run {
				binder.onBindScopedModifier(modifier)
			}
		}
	}

	//endregion

	//region Callback Binding

	private val boundCallbackIds = HashMap<CallbackRouter, Int>()
	private val boundCallbacks = FastIntMap<CallbackRouter>()

	private val callbackBinder = MapComputeFunction<CallbackRouter, Int> { callback ->
		obtainCallbackId_inline().also { newId ->
			boundCallbacks[newId] = callback
		}
	}

	internal fun forceUnbindCallback(callbackId: Int) {
		val callback = boundCallbacks.set(callbackId, null)
		if (callback != null) {
			boundCallbackIds.remove(callback)
			recycleCallbackId(callbackId)
			return
		}
		throw AssertionError("Callback already unbound")
	}

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

	//endregion
}

private fun E_IdGenOverflow() = Error("Overflow: ID generation already exhausted")
