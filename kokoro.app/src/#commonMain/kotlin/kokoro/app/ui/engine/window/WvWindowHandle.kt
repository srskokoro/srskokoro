package kokoro.app.ui.engine.window

import androidx.collection.IntObjectMap
import androidx.collection.MutableIntList
import androidx.collection.MutableIntObjectMap
import androidx.collection.MutableScatterSet
import androidx.collection.ScatterSet
import kokoro.internal.AutoCloseable2
import kokoro.internal.DEPRECATION_ERROR
import kokoro.internal.NOTHING_TO_INLINE
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kokoro.internal.assertUnreachable
import kotlin.jvm.JvmField

@MainThread
class WvWindowHandle : AutoCloseable2 {
	private var id_: Int
	val id: Int get() = id_

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@JvmField internal var attachment: WvWindowHandleAttachment? = null

	private var parent_: WvWindowHandle?
	val parent: WvWindowHandle? get() = parent_

	private val children_ = MutableScatterSet<WvWindowHandle>()
	val children: ScatterSet<WvWindowHandle> get() = children_

	@Suppress("ConvertSecondaryConstructorToPrimary")
	@AnyThread
	private constructor(id: Int, parent: WvWindowHandle?) {
		assert({ id > INVALID_ID })
		this.id_ = id
		this.parent_ = parent
	}

	@Suppress(NOTHING_TO_INLINE)
	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	internal inline fun detach() {
		@Suppress(DEPRECATION_ERROR)
		attachment = null
	}

	override fun close() {
		assertThreadMain()

		// NOTE: The code hereafter is expected to be idempotent after success,
		// i.e., multiple invocations of this `close()` method shouldn't affect
		// anything once the object is already considered successfully closed.
		// --

		children_.removeIf { child ->
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

		@Suppress(DEPRECATION_ERROR)
		attachment?.let { attachment ->
			WvWindowHandle_destroy_attachment(attachment) // May throw
			detach() // Expected to never throw
		}

		// NOTE: The code hereafter must not throw.
		// --

		parent_?.let { parent ->
			parent.children_.remove(this)
			parent_ = null
		}

		val id = id_
		if (id == INVALID_ID) return // Already closed before.
		// NOTE: The operation below not only marks the object as "closed" but
		// also prevents multiple `close()` calls from affecting the globals as
		// the code afterwards would.
		id_ = INVALID_ID

		globalMap_.remove(id)
		recycledIds.add(id)
	}

	val isClosed: Boolean
		inline get() = id == INVALID_ID

	companion object {
		const val INVALID_ID = 0

		private val globalMap_ = MutableIntObjectMap<WvWindowHandle>()
		val globalMap: IntObjectMap<WvWindowHandle> = globalMap_

		private val recycledIds = MutableIntList()
		private var lastId = INVALID_ID

		@MainThread
		private fun nextId(): Int {
			assertThreadMain()

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

		@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
		@MainThread
		internal fun recycleId(id: Int) {
			assertThreadMain()

			assert({ id > INVALID_ID && id !in globalMap_ })
			if (id > lastId) lastId = id

			recycledIds.add(id)
		}

		@MainThread
		fun create(parent: WvWindowHandle?): WvWindowHandle {
			val id = nextId() // Expected to assert proper thread

			val new = WvWindowHandle(id, parent)
			globalMap_[id] = new

			parent?.children_?.add(new)
			return new
		}

		@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
		@MainThread
		fun create(id: Int, parent: WvWindowHandle?): WvWindowHandle? {
			assertThreadMain()

			val new = WvWindowHandle(id, parent)
			globalMap_.put(id, new)?.let { prev ->
				globalMap_[id] = prev // Restore
				assertUnreachable(orFailWith = { "ID already in use: $id" })
				globalMap_[id] = prev // Restore
				return null
			}
			if (id > lastId) lastId = id

			parent?.children_?.add(new)
			return new
		}
	}
}
