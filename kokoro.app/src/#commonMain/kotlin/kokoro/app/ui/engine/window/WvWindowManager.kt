package kokoro.app.ui.engine.window

import androidx.annotation.GuardedBy
import androidx.collection.MutableScatterMap
import androidx.collection.MutableScatterSet
import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.selects.SelectClause0
import kotlin.jvm.JvmField

@OptIn(nook::class)
abstract class WvWindowManager @nook protected constructor(
	id: WvWindowId?,
	parent: WvWindowManager?,
) {
	/**
	 * - WARNING: Must only be modified from the main thread.
	 * - CONTRACT: `null` on [close].
	 */
	@JvmField @nook var id_: WvWindowId? = id
	val id: WvWindowId? inline get() = id_

	@Suppress("NOTHING_TO_INLINE")
	inline fun getIdOrThrow() = id ?: throw E_Closed()

	/**
	 * - WARNING: Must only be modified from the main thread.
	 * - CONTRACT: `null` on [close].
	 */
	@JvmField @nook var parent_: WvWindowManager? = parent
	val parent: WvWindowManager? inline get() = parent_

	/** WARNING: Must only be accessed (and modified) from the main thread. */
	private val children = MutableScatterSet<WvWindowManager>(0)

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	@Throws(LoadConflictException::class)
	inline fun load(id: WvWindowId) = load(id, this)

	// --

	@MainThread
	fun launch() {
		if (!launchOrReject()) {
			throw E_Closed()
		}
	}

	@MainThread
	abstract fun launchOrReject(): Boolean

	@MainThread
	fun <T> post(bus: UiBus<T>, value: T) {
		if (!postOrDiscard(bus, value)) {
			throw E_Closed()
		}
	}

	@MainThread
	abstract fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean

	// --

	protected abstract val closeJob: Job

	val isOpen inline get() = id_ != null

	val isClosed inline get() = id_ == null

	@MainThread
	fun close() {
		assertThreadMain()

		val id = id_ ?: return // Already closed before

		children.removeIf { child ->
			assert({ child.parent_.let { it == null || it === this } })
			child.parent_ = null // Prevent child from removing itself
			try {
				child.close()
			} catch (ex: Throwable) {
				child.parent_ = this // Revert
				throw ex
			}
			true // Remove child
		}

		id_ = null // Mark as closed now (so that we don't get called again)
		try {
			onClose()
		} catch (ex: Throwable) {
			id_ = id // Revert
			throw ex
		}

		// NOTE: The code hereafter must not throw.
		// --

		parent_?.let { parent ->
			parent.children.remove(this)
			parent_ = null
		}

		synchronized(openHandles_lock) {
			val prev = openHandles.remove(id)
			assert({ prev === this })
		}
	}

	/** CONTRACT: Must cause [closeJob] to complete. */
	@MainThread
	protected abstract fun onClose()

	/**
	 * @see awaitClose
	 * @see onAwaitClose
	 */
	fun invokeOnClose(handler: CompletionHandler) =
		closeJob.invokeOnCompletion(handler)

	/**
	 * Suspends until [close]`()` is called.
	 *
	 * @see invokeOnClose
	 * @see onAwaitClose
	 */
	suspend fun awaitClose() = closeJob.join()

	/**
	 * @see awaitClose
	 * @see invokeOnClose
	 */
	val onAwaitClose: SelectClause0 get() = closeJob.onJoin

	// --

	companion object {
		@nook const val E_Closed = "Already closed (or invalid)"
		@nook fun E_Closed() = IllegalStateException(E_Closed)

		private val openHandles_lock = SynchronizedObject()
		@GuardedBy("openHandles_lock") private val openHandles = MutableScatterMap<WvWindowId, WvWindowHandle>()

		@AnyThread
		fun get(id: WvWindowId): WvWindowHandle? = synchronized(openHandles_lock) { openHandles[id] }

		@AnyThread
		fun get(id: WvWindowId, parent: WvWindowManager?): WvWindowHandle? {
			val h = get(id)
			if (h != null && h.parent === parent) {
				return h
			}
			return null
		}

		@MainThread
		@Throws(LoadConflictException::class)
		fun load(id: WvWindowId, parent: WvWindowManager?): WvWindowHandle {
			assertThreadMain()

			if (parent == null || parent.isOpen) kotlin.run {
				synchronized(openHandles_lock) {
					openHandles.compute(id) { _, old ->
						if (old != null) return@run old
						WvWindowHandle(id, parent)
					}
				}.let { new ->
					parent?.children?.add(new) // Requires the main thread
					return new
				}
			}.let { old ->
				if (old.parent === parent) {
					return old
				}
				throw LoadConflictException(old, parent)
			} else return closed()
		}

		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun closed(): WvWindowHandle = WvWindowHandle.closed()
	}

	class LoadConflictException @nook constructor(
		@JvmField val target: WvWindowHandle,
		@JvmField val targetParent: WvWindowManager?,
	) : IllegalStateException() {
		override val message: String
			get() = "Target: $target; Target parent: ${targetParent?.id}"
	}

	override fun toString(): String = "${
		if (this is WvWindowHandle) "WvWindowHandle"
		else super.toString()
	}(id=$id, parent=${parent?.id})"
}

@Suppress("NOTHING_TO_INLINE")
@AnyThread
inline fun WvWindowHandle.Companion.get(id: WvWindowId) = WvWindowManager.get(id)

@Suppress("NOTHING_TO_INLINE")
@AnyThread
inline fun WvWindowHandle.Companion.get(id: WvWindowId, parent: WvWindowManager?) = WvWindowManager.get(id, parent)

@Suppress("NOTHING_TO_INLINE")
@MainThread
@Throws(WvWindowManager.LoadConflictException::class)
inline fun WvWindowHandle.Companion.load(id: WvWindowId, parent: WvWindowManager?) = WvWindowManager.load(id, parent)
