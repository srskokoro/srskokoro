package kokoro.app.ui.engine.window

import androidx.annotation.GuardedBy
import androidx.collection.MutableScatterMap
import androidx.collection.MutableScatterSet
import kokoro.app.ui.engine.UiBus
import kokoro.internal.AutoCloseable2
import kokoro.internal.DEBUG
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.require
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

@OptIn(nook::class)
class WvWindowHandle @AnyThread private constructor(
	id: String?,
	windowFactoryId: WvWindowFactoryId,
	parent: WvWindowHandle?,
) : WvWindowHandleBasis(id, windowFactoryId, parent), AutoCloseable2 {

	val parent: WvWindowHandle? inline get() = parent_

	/** WARNING: Must only be accessed (and modified) from the main thread. */
	private val children = MutableScatterSet<WvWindowHandle>()

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	@Throws(IdConflictException::class)
	inline fun create(
		id: String,
		windowFactoryId: WvWindowFactoryId,
	) = create(id, windowFactoryId, this)

	@MainThread
	fun <T> post(bus: UiBus<T>, value: T) {
		if (!postOrDiscard(bus, value)) {
			throw E_Closed()
		}
	}

	@MainThread
	override fun close() {
		assertThreadMain()

		val id = id ?: return // Already closed before

		children.removeIf { child ->
			assert({ child.parent_.let { it == null || it == this } })
			child.parent_ = null // Prevent child from removing itself
			try {
				child.close()
			} catch (ex: Throwable) {
				child.parent_ = this // Restore
				throw ex
			}
			true // Remove child
		}

		onClose() // May throw
		assert({ isClosed }, or = {
			"Unexpected: `${::onClose.name}()` should cause `${::isClosed.name}` to become true."
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
			parent: WvWindowHandle?,
		): WvWindowHandle {
			assertThreadMain()

			if (DEBUG) require(WvWindowFactory.get(windowFactoryId) != null, or = {
				"Window factory ID not registered: $windowFactoryId"
			})

			return synchronized(openHandles_lock) {
				openHandles.compute(id) { _, v ->
					if (v != null) throw IdConflictException(v)
					WvWindowHandle(id, windowFactoryId, parent)
				}
			}.also { h ->
				parent?.children?.add(h) // Requires the main thread
			}
		}
	}

	class IdConflictException(val oldHandle: WvWindowHandle) : IllegalStateException(
		"Handle ID already in use: ${oldHandle.id}"
	)
}
