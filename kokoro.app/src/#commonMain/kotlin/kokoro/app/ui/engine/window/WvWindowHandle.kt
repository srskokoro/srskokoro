package kokoro.app.ui.engine.window

import androidx.collection.IntObjectMap
import androidx.collection.MutableIntList
import androidx.collection.MutableIntObjectMap
import androidx.collection.MutableScatterSet
import kokoro.internal.AutoCloseable2
import kokoro.internal.DEPRECATION_ERROR
import kokoro.internal.NOTHING_TO_INLINE
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.annotation.MainThread
import kokoro.internal.assert
import kokoro.internal.assertThreadMain
import kotlin.jvm.JvmField

@MainThread
class WvWindowHandle : AutoCloseable2 {
	private val id: Int

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@JvmField internal var attachment: WvWindowHandleAttachment? = null

	val parent get() = parent_
	private var parent_: WvWindowHandle?
	private val children = MutableScatterSet<WvWindowHandle>()

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	internal constructor(id: Int, parent: WvWindowHandle?) {
		assertThreadMain()

		assert({ id > 0 })
		if (id > lastId) lastId = id

		this.id = id
		globalMap_.put(id, this)

		this.parent_ = parent
		parent?.children?.add(this)
	}

	constructor(parent: WvWindowHandle?) {
		val id = nextId()
		this.id = id
		globalMap_.put(id, this)

		this.parent_ = parent
		parent?.children?.add(this)
	}

	@Suppress(NOTHING_TO_INLINE)
	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	internal inline fun detach() {
		@Suppress(DEPRECATION_ERROR)
		attachment = null
	}

	override fun close() {
		assertThreadMain()

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

		@Suppress(DEPRECATION_ERROR)
		attachment?.let { attachment ->
			WvWindowHandle_destroy_attachment(attachment) // May throw
			detach() // Expected to never throw
		}

		// --
		// NOTE: The code hereafter must not throw.

		parent_?.let { parent ->
			parent.children.remove(this)
			parent_ = null
		}

		val id = id
		globalMap_.remove(id)
		recycledIds.add(id)
	}

	companion object {
		private val globalMap_ = MutableIntObjectMap<WvWindowHandle>()
		val globalMap: IntObjectMap<WvWindowHandle> = globalMap_

		private val recycledIds = MutableIntList()
		private var lastId = 0

		private fun nextId(): Int {
			assertThreadMain()

			recycledIds.run {
				val l = lastIndex
				if (l >= 0) {
					return removeAt(l)
				}
			}

			val id = ++lastId
			if (id <= 0) {
				lastId--
				throw Error("Overflow: maximum ID exceeded")
			}
			return id
		}
	}
}
