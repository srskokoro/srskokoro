package kokoro.app.ui

import TODO
import androidx.compose.runtime.*
import app.cash.redwood.compose.RedwoodComposition
import kokoro.app.ui.wv.widget.WvWidgetChildren
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import kotlin.coroutines.Continuation

open class RedwoodAppWindow : AppWindow {
	constructor() : super()
	constructor(gc: GraphicsConfiguration?) : super(gc)
	constructor(title: String?) : super(title)
	constructor(title: String?, gc: GraphicsConfiguration?) : super(title, gc)

	private var _composition: RedwoodComposition? = null
	val composition: RedwoodComposition
		get() = _composition ?: error("Must first call `${::init.name}()`")

	/** @see composition */
	fun init(mainScope: CoroutineScope) {
		if (_composition != null) {
			error("Already initialized")
		}
		_composition = RedwoodComposition(
			mainScope + _frameClock + super.ref,
			TODO { PLACEHOLDER<WvWidgetChildren>() },
			TODO { PLACEHOLDER() },
		)
	}

	//region Frame Clock Handling

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
		private var awaiters = mutableListOf<FrameAwaiter<*>>()
		private var spareList = mutableListOf<FrameAwaiter<*>>()

		fun sendFrame(timeNanos: Long) {
			synchronized(lock) {
				// Rotate the lists so that if a resumed continuation on an
				// immediate dispatcher bound to the thread calling `sendFrame()`
				// immediately awaits again we don't disrupt iteration of
				// resuming the rest.
				val toResume = awaiters
				awaiters = spareList
				spareList = toResume

				for (i in 0 until toResume.size) {
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

	//endregion
}
