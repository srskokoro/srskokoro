package kokoro.app.ui.wv

import TODO
import assertUnreachable
import kokoro.app.ui.wv.widget.WvWidget
import kokoro.internal.collections.MapComputeFunction
import kokoro.internal.collections.computeIfAbsent
import korlibs.datastructure.FastIntMap
import korlibs.datastructure.IntDeque

class WvBinder {
	internal val bindingCommand = StringBuilder()

	private var deferredException: Throwable? = null

	fun deferException(ex: Throwable) {
		val prev = deferredException
		if (prev == null) {
			deferredException = ex
		} else {
			prev.addSuppressed(ex)
		}
	}

	//region Widget Binding

	internal val widgetIdPool = IntDeque()
	internal var widgetIdLastGen = 0

	internal val widgetStatusChanges = ArrayList<WvWidget>()

	fun concludeChanges() {
		val cmd = bindingCommand
		val statusChanges = widgetStatusChanges

		for (i in statusChanges.indices) {
			val widget = statusChanges[i]

			val status = widget._widgetStatus
			widget._widgetStatus = 0 // Consume

			if (status and WS_GARBAGE != 0) {
				val widgetId = widget._widgetId
				if (widgetId == 0) {
					assertUnreachable { "Widget already unbound" }
					continue
				}

				cmd.append("D$(")
				cmd.append(widgetId)
				cmd.append(")\n")

				// NOTE: The fact that we recycle the widget IDs right away,
				// without even waiting for the "unbind" command to be evaluated
				// by the `WebView`, is the reason we shouldn't expose those
				// widget IDs to the API clients, i.e., the widget updaters, the
				// modifier binders, etc., so that they may not accidentally
				// manipulate a stale ID.
				widgetIdPool.addLast(widgetId) // Recycle ID
				continue // Already unbound. Nothing to do.
			}

			if (status and WS_UPDATE != 0) {
				TODO { IMPLEMENT }
			}
		}

		// Done. Everything already processed.
		statusChanges.clear()

		deferredException?.let { ex ->
			deferredException = null
			throw ex
		}
	}

	//endregion

	//region Callback Binding

	private val callbackIdPool = IntDeque()
	private var callbackIdLastGen = 0

	private val boundCallbackIds = HashMap<CallbackRouter, Int>()
	private val boundCallbacks = FastIntMap<CallbackRouter>()

	private val callbackBinder = MapComputeFunction<CallbackRouter, Int> { callback ->
		val idPool = callbackIdPool
		val newId: Int
		if (idPool.isEmpty()) {
			newId = callbackIdLastGen + 1
			if (newId <= 0) throw E_IdGenOverflow()
			callbackIdLastGen = newId
		} else {
			newId = idPool.removeFirst()
		}
		boundCallbacks[newId] = callback
		newId
	}

	internal fun forceUnbindCallback(callbackId: Int) {
		val callback = boundCallbacks.set(callbackId, null)
		if (callback != null) {
			boundCallbackIds.remove(callback)
			callbackIdPool.addLast(callbackId) // Recycle ID
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

internal fun E_IdGenOverflow() = Error("Overflow: ID generation already exhausted")
