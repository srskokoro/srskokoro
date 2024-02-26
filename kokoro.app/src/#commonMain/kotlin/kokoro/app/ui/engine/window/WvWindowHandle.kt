package kokoro.app.ui.engine.window

import androidx.annotation.GuardedBy
import androidx.annotation.IntRange
import androidx.collection.MutableIntList
import androidx.collection.MutableIntObjectMap
import androidx.collection.MutableScatterSet
import kokoro.internal.AutoCloseable2
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.assertUnreachable
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

@MainThread
abstract class WvWindowHandle @AnyThread constructor(parent: WvWindowHandle?) : AutoCloseable2 {
	/** WARNING: Must only be modified from the main thread. */
	private var id_: Int = INVALID_ID
	val id: Int get() = id_

	/** WARNING: Must only be modified from the main thread. */
	private var parent_: WvWindowHandle? = parent
	val parent: WvWindowHandle? get() = parent_

	/** WARNING: Must only be accessed (and modified) from the main thread. */
	private val children = MutableScatterSet<WvWindowHandle>()

	@MainThread
	protected abstract fun onClose()

	@MainThread
	override fun close() {
		assertThreadMain()

		val id = id_
		if (id == INVALID_ID) return // Already closed before

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

		// NOTE: The code hereafter must not throw.
		// --

		parent_?.let { parent ->
			parent.children.remove(this)
			parent_ = null
		}

		// NOTE: The operation below not only marks the object as "closed" but
		// also prevents multiple `close()` calls from affecting the globals as
		// the code afterwards would.
		id_ = INVALID_ID

		synchronized(globalLock) {
			val prev = openHandles.remove(id)
			if (prev == this) {
				recycledIds.add(id)
				return // Skip code below
			}
			if (prev != null) openHandles[id] = prev // Restore
		}
		assertUnreachable(or_fail_with = { "Unexpected open handle with ID $id" })
	}

	val isClosed: Boolean inline get() = isClose(id)
	val isOpen: Boolean inline get() = isOpen(id)

	companion object {
		const val INVALID_ID = 0

		@Suppress("NOTHING_TO_INLINE") inline fun isClose(id: Int): Boolean = id == INVALID_ID
		@Suppress("NOTHING_TO_INLINE") inline fun isOpen(id: Int): Boolean = id != INVALID_ID

		// --

		private val globalLock = SynchronizedObject()
		@GuardedBy("globalLock") private val openHandles = MutableIntObjectMap<WvWindowHandle>()
		@GuardedBy("globalLock") private val recycledIds = MutableIntList()
		@GuardedBy("globalLock") private var lastId = INVALID_ID

		@GuardedBy("globalLock")
		private fun nextId_unsafe(): Int {
			recycledIds.run {
				val l = lastIndex
				if (l >= 0) {
					return removeAt(l)
				}
			}

			val id = ++lastId
			if (id <= INVALID_ID) {
				lastId--
				throw Error("Overflow: maximum ID exceeded")
			}
			return id
		}

		//--

		@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
		@AnyThread
		@PublishedApi internal fun get_(id: Int): WvWindowHandle? {
			synchronized(globalLock) {
				return openHandles[id]
			}
		}

		@AnyThread
		inline fun <reified T : WvWindowHandle> get(id: Int): T? {
			@Suppress("DEPRECATION_ERROR")
			return get_(id) as? T
		}

		@MainThread
		fun open(handle: WvWindowHandle) {
			assertThreadMain()

			synchronized(globalLock) {
				val id = nextId_unsafe()
				openHandles[id] = handle
				initialize(handle, id) // Requires the main thread
			}
		}

		@MainThread
		fun openAt(@IntRange(from = INVALID_ID + 1L) id: Int, handle: WvWindowHandle): Boolean {
			assertThreadMain()

			assert({ id > INVALID_ID })
			synchronized(globalLock) {
				val prev = openHandles.put(id, handle)
				if (prev == null) {
					if (id > lastId) lastId = id
					initialize(handle, id) // Requires the main thread
					return true
				} else {
					openHandles[id] = prev // Restore
					assertUnreachable(or_fail_with = { "ID already in use: $id" })
					return false
				}
			}
		}

		/**
		 * WARNING: May be called inside a synchronization block.
		 */
		@Suppress("NOTHING_TO_INLINE")
		@MainThread
		private inline fun initialize(handle: WvWindowHandle, @IntRange(from = INVALID_ID + 1L) id: Int) {
			assertThreadMain()

			handle.id_ = id
			handle.parent_?.children?.add(handle)
		}
	}
}

internal expect class WvWindowHandleImpl : WvWindowHandle {

	override fun onClose()

	companion object
}
