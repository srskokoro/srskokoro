package kokoro.app.ui.engine.window

import androidx.annotation.GuardedBy
import androidx.collection.MutableScatterMap
import androidx.collection.MutableScatterSet
import kokoro.app.ui.engine.UiBus
import kokoro.internal.DEBUG
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.require
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.selects.SelectClause0
import kotlin.jvm.JvmField

@OptIn(nook::class)
abstract class WvWindowManager @nook constructor(
	val windowFactoryId: WvWindowFactoryId,
	parent: WvWindowManager?,
) {
	abstract val id: String?

	/**
	 * - WARNING: Must only be modified from the main thread.
	 * - CONTRACT: `null` on [close].
	 */
	@JvmField @nook var parent_: WvWindowManager? = parent
	val parent: WvWindowManager? inline get() = parent_

	/** WARNING: Must only be accessed (and modified) from the main thread. */
	private val children = MutableScatterSet<WvWindowManager>()

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	@Throws(IdConflictException::class)
	inline fun create(
		id: String,
		windowFactoryId: WvWindowFactoryId,
	) = create(id, windowFactoryId, this)

	// --

	@MainThread
	abstract fun launch()

	@MainThread
	fun <T> post(bus: UiBus<T>, value: T) {
		if (!postOrDiscard(bus, value)) {
			throw E_Closed()
		}
	}

	@MainThread
	abstract fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean

	// --

	val isOpen inline get() = !isClosed

	abstract val isClosed: Boolean

	/**
	 * CONTRACT: This method is expected to cause [id] to become `null` and
	 * [isClosed] to be `true`.
	 */
	@MainThread
	protected abstract fun onClose()

	/**
	 * @see awaitClose
	 * @see onAwaitClose
	 */
	abstract fun invokeOnClose(handler: (WvWindowHandle) -> Unit): DisposableHandle

	/**
	 * Suspends until [close]`()` is called.
	 *
	 * @see invokeOnClose
	 * @see onAwaitClose
	 */
	abstract suspend fun awaitClose()

	/**
	 * @see awaitClose
	 * @see invokeOnClose
	 */
	abstract val onAwaitClose: SelectClause0

	/**
	 * @see awaitClose
	 * @see invokeOnClose
	 */
	@MainThread
	fun close() {
		assertThreadMain()

		val id = this.id ?: return // Already closed before

		children.removeIf { child ->
			assert({ child.parent_.let { it == null || it == this } })
			child.parent_ = null // Prevent child from removing itself
			try {
				child.close()
			} catch (ex: Throwable) {
				child.parent_ = this // Revert
				throw ex
			}
			true // Remove child
		}

		onClose() // May throw
		assert({ this.id == null && isClosed }, or = {
			"Unexpected: `${::onClose.name}()` contract not fulfilled."
		})

		// NOTE: The code hereafter must not throw.
		// --

		parent_?.let { parent ->
			parent.children.remove(this)
			parent_ = null
		}

		synchronized(openHandles_lock) {
			val prev = openHandles.remove(id)
			assert({ prev == this })
		}
	}

	companion object {
		@nook const val E_CLOSED = "Already closed (or invalid)"
		@nook fun E_Closed() = IllegalStateException(E_CLOSED)

		private val openHandles_lock = SynchronizedObject()
		@GuardedBy("openHandles_lock") private val openHandles = MutableScatterMap<String, WvWindowHandle>()

		@AnyThread
		fun get(id: String): WvWindowHandle? = synchronized(openHandles_lock) { openHandles[id] }

		@AnyThread
		fun get(id: String, windowFactoryId: WvWindowFactoryId): WvWindowHandle? {
			val h = get(id)
			if (h != null && h.windowFactoryId == windowFactoryId) {
				return h
			}
			return null
		}

		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun createClosed() = createClosed(WvWindowFactoryId.NOTHING)

		@AnyThread
		fun createClosed(windowFactoryId: WvWindowFactoryId) = WvWindowHandle(
			id = null,
			windowFactoryId,
			parent = null,
		)

		@MainThread
		@Throws(IdConflictException::class)
		fun create(
			id: String,
			windowFactoryId: WvWindowFactoryId,
			parent: WvWindowManager?,
		): WvWindowHandle {
			assertThreadMain()

			if (DEBUG) require(WvWindowFactory.get(windowFactoryId) != null, or = {
				"Window factory ID not registered: $windowFactoryId"
			})

			return if (parent == null || parent.isOpen) {
				synchronized(openHandles_lock) {
					openHandles.compute(id) { _, v ->
						if (v != null) throw IdConflictException(v)
						WvWindowHandle(id, windowFactoryId, parent)
					}
				}.also { h ->
					parent?.children?.add(h) // Requires the main thread
				}
			} else createClosed(windowFactoryId)
		}
	}

	class IdConflictException @nook constructor(val oldHandle: WvWindowHandle) : IllegalStateException(
		"Handle ID already in use: ${oldHandle.id}"
	)
}

@Suppress("NOTHING_TO_INLINE")
@AnyThread
inline fun WvWindowHandle.Companion.get(id: String) = WvWindowManager.get(id)

@Suppress("NOTHING_TO_INLINE")
@AnyThread
inline fun WvWindowHandle.Companion.get(id: String, windowFactoryId: WvWindowFactoryId) =
	WvWindowManager.get(id, windowFactoryId)

@Suppress("NOTHING_TO_INLINE")
@AnyThread
inline fun WvWindowHandle.Companion.createClosed() = WvWindowManager.createClosed()

@Suppress("NOTHING_TO_INLINE")
@AnyThread
inline fun WvWindowHandle.Companion.createClosed(windowFactoryId: WvWindowFactoryId) =
	WvWindowManager.createClosed(windowFactoryId)

@Suppress("NOTHING_TO_INLINE")
@MainThread
@Throws(WvWindowManager.IdConflictException::class)
inline fun WvWindowHandle.Companion.create(
	id: String,
	windowFactoryId: WvWindowFactoryId,
	parent: WvWindowManager?,
) = WvWindowManager.create(id, windowFactoryId, parent)
