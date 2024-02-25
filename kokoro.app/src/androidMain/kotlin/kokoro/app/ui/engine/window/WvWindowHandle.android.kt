package kokoro.app.ui.engine.window

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kokoro.app.CoreApplication
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.check

@MainThread
internal actual class WvWindowHandleImpl @AnyThread constructor(parent: WvWindowHandle?) : WvWindowHandle(parent), Parcelable {
	private var context: Any? = null

	@Suppress("NOTHING_TO_INLINE")
	inline fun attachContext(activity: WvWindowActivity) {
		this.context = activity
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun attachContext(task: ActivityManager.AppTask) {
		this.context = task
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun detachContext() {
		this.context = null
	}

	private var uri_: Uri? = null
	private val uri: Uri
		@MainThread get() {
			assertThreadMain()

			var r = uri_
			if (r != null) return r

			val id = id
			check(isOpen(id), or_fail_with = { "Already closed (or not yet opened)" })

			r = Uri.parse("$URI_SCHEME_to_ID_HEX:${Integer.toHexString(id)}")
			uri_ = r

			return r
		}

	/**
	 * @see WvWindowHandleImpl.Companion.get
	 */
	@MainThread
	fun toIntent() = Intent(CoreApplication.get(), WvWindowActivity::class.java).apply {
		data = uri // Requires the main thread. May throw on a closed handle.
		putExtra(EXTRAS_KEY_to_ID_INT, id)
		parent?.let { p -> putExtra(EXTRAS_KEY_to_PARENT_ID_INT, p.id) }
	}

	@MainThread
	actual override fun onClose() {
		uri_ = null
		when (val c = context) {
			null -> return // Skip code below
			is Activity -> c.finishAndRemoveTask()
			is ActivityManager.AppTask -> c.finishAndRemoveTask()
			else -> throw AssertionError("Unexpected: $c")
		}
		detachContext()
	}

	companion object {
		private const val URI_SCHEME_to_ID_HEX = "x:" // The 'x' stands for "hexadecimal"
		private const val EXTRAS_KEY_to_ID_INT = "id"
		private const val EXTRAS_KEY_to_PARENT_ID_INT = "parentId"

		/**
		 * @see WvWindowHandleImpl.get
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun getId(intent: Intent): Int =
			intent.getIntExtra(EXTRAS_KEY_to_ID_INT, INVALID_ID)

		/**
		 * @see WvWindowHandleImpl.getId
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun getParentId(intent: Intent): Int =
			intent.getIntExtra(EXTRAS_KEY_to_PARENT_ID_INT, INVALID_ID)

		/**
		 * @see WvWindowHandleImpl.toIntent
		 * @see WvWindowHandleImpl.getId
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun get(intent: Intent): WvWindowHandleImpl? = get(getId(intent))

		// --

		@Suppress("unused")
		@JvmField val CREATOR = object : Parcelable.Creator<WvWindowHandleImpl> {
			override fun newArray(size: Int) = arrayOfNulls<WvWindowHandleImpl?>(size)

			@AnyThread
			override fun createFromParcel(parcel: Parcel): WvWindowHandleImpl =
				get(parcel.readInt()) ?: WvWindowHandleImpl(null)
		}
	}

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(id)
	}

	override fun describeContents(): Int = 0
}
