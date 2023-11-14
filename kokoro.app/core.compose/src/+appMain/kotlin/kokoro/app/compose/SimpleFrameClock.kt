package kokoro.app.compose

import androidx.compose.runtime.*
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation

/** @see androidx.compose.runtime.BroadcastFrameClock */
abstract class SimpleFrameClock : MonotonicFrameClock {

	private class FrameAwaiter<R>(
		val onFrame: (Long) -> R,
		val continuation: Continuation<R>,
	) {
		fun resume(timeNanos: Long) {
			continuation.resumeWith(runCatching { onFrame(timeNanos) })
		}
	}

	private val lock = SynchronizedObject()
	private var awaiters = ArrayList<FrameAwaiter<*>>()
	private var spareList = ArrayList<FrameAwaiter<*>>()

	/**
	 * Send a frame for time [timeNanos] to all current callers of [withFrameNanos].
	 * The `onFrame` callback for each caller is invoked synchronously during
	 * the call to [sendFrame].
	 */
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
		onNewAwaiters()
	}

	/**
	 * Called whenever the number of awaiters has changed from 0 to 1.
	 */
	protected abstract fun onNewAwaiters()
}
