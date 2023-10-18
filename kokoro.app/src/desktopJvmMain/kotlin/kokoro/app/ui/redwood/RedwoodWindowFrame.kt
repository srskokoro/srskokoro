package kokoro.app.ui.redwood

import androidx.compose.runtime.*
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.Widget
import kokoro.app.ui.BaseWindowFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.plus
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import kotlin.coroutines.Continuation

open class RedwoodWindowFrame : BaseWindowFrame {
	constructor() : super()
	constructor(gc: GraphicsConfiguration?) : super(gc)
	constructor(title: String?) : super(title)
	constructor(title: String?, gc: GraphicsConfiguration?) : super(title, gc)

	private var _composition: RedwoodComposition? = null
	val composition: RedwoodComposition
		get() = _composition ?: error("Must first call `init()`")

	/** @see composition */
	fun <W : Any> init(
		mainScope: CoroutineScope,
		view: RedwoodView<W>,
		provider: Widget.Provider<W>,
		onEndChanges: () -> Unit = {},
	) {
		_composition?.cancel()
		_composition = RedwoodComposition(
			wrapMainScope(mainScope),
			view, provider, onEndChanges,
		)
	}

	/** @see composition */
	fun <W : Any> init(
		mainScope: CoroutineScope,
		container: Widget.Children<W>,
		uiConfigurations: StateFlow<UiConfiguration>,
		provider: Widget.Provider<W>,
		onEndChanges: () -> Unit = {},
	) {
		_composition?.cancel()
		_composition = RedwoodComposition(
			wrapMainScope(mainScope),
			container, uiConfigurations,
			provider, onEndChanges,
		)
	}

	private fun wrapMainScope(mainScope: CoroutineScope) =
		mainScope + (_frameClock + super.ref)

	//#region Frame Clock Handling

	private val _frameClock = FrameClock { repaint() }
	val frameClock: MonotonicFrameClock get() = _frameClock

	private val nanoTimeStart = System.nanoTime()

	override fun paint(g: Graphics?) {
		super.paint(g)
		_frameClock.sendFrame(System.nanoTime() - nanoTimeStart)
	}

	/** @see androidx.compose.runtime.BroadcastFrameClock */
	private class FrameClock(
		private val onFrameNanosSwing: Runnable,
	) : MonotonicFrameClock {

		private val lock = Any()
		private var awaiters = ArrayList<FrameAwaiter<*>>()
		private var spareList = ArrayList<FrameAwaiter<*>>()

		fun sendFrame(timeNanos: Long) {
			synchronized(lock) {
				// Rotate the lists so that if a resumed continuation on an
				// immediate dispatcher bound to the thread calling `sendFrame()`
				// immediately awaits again we don't disrupt iteration of
				// resuming the rest.
				val toResume = awaiters
				awaiters = spareList
				spareList = toResume

				for (i in toResume.indices) {
					toResume[i].resume(timeNanos)
				}
				toResume.clear()
			}
		}

		override suspend fun <R> withFrameNanos(
			onFrame: (frameTimeNanos: Long) -> R
		): R = suspendCancellableCoroutine { co ->
			val awaiter = FrameAwaiter(onFrame, co)
			synchronized(lock) {
				awaiters.add(awaiter)
			}
			co.invokeOnCancellation {
				synchronized(lock) {
					awaiters.remove(awaiter)
				}
			}
			EventQueue.invokeLater(onFrameNanosSwing)
		}
	}

	private class FrameAwaiter<R>(
		val onFrame: (Long) -> R,
		val continuation: Continuation<R>,
	) {
		fun resume(timeNanos: Long) {
			continuation.resumeWith(runCatching { onFrame(timeNanos) })
		}
	}

	//#endregion
}
