package kokoro.app.ui.engine.window

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kokoro.app.CoreApplication
import kokoro.app.ui.engine.UiBus
import kokoro.internal.ASSERTIONS_ENABLED
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.errorAssertion
import kokoro.internal.os.SerializationEncoded

@MainThread
internal actual class WvWindowHandleImpl @AnyThread constructor(parent: WvWindowHandle?) : WvWindowHandle(parent), Parcelable {
	private var context: Any? = null

	val activity get() = context as? WvWindowActivity

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

	// --

	private var uri_: Uri? = null
	private val uri: Uri
		@MainThread get() {
			assertThreadMain()

			var r = uri_
			if (r != null) return r

			val id = id
			ensureOpen(id)

			r = Uri.parse("$URI_SCHEME_to_ID_HEX:${Integer.toHexString(id)}")
			uri_ = r

			return r
		}

	/**
	 * @see WvWindowHandle.launch
	 * @see WvWindowHandleImpl.Companion.get
	 * @see WvWindowFactory.id
	 */
	@MainThread
	private fun newLaunchIntent(app: Application, windowFactoryId: String) = Intent(app, WvWindowActivity::class.java).apply {
		data = uri // Requires the main thread. May throw on a closed handle.
		addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Necessary or `startActivity()` will fail

		putExtra(EXTRAS_KEY_to_ID, id)
		parent?.let { p -> putExtra(EXTRAS_KEY_to_PARENT_ID, p.id) }
		putExtra(EXTRAS_KEY_to_WINDOW_FACTORY_ID, windowFactoryId)
	}

	/**
	 * @see WvWindowHandle.post
	 * @see WvWindowHandleImpl.Companion.getPostBusId
	 * @see WvWindowHandleImpl.Companion.getPostPayload
	 */
	@MainThread
	private fun <T> newPostIntent(app: Application, bus: UiBus<T>, payload: T) = Intent(app, WvWindowActivity::class.java).apply {
		data = uri // Requires the main thread. May throw on a closed handle.
		putExtra(EXTRAS_KEY_to_POST_BUS_ID, bus.id)
		SerializationEncoded(payload, bus.serialization)
			.putInto(this, EXTRAS_KEY_to_POST_PAYLOAD)
	}

	// --

	@MainThread
	actual override fun launch(windowFactoryId: String): WvWindowHandle = launch(windowFactoryId, null)

	/**
	 * @throws IllegalStateException
	 *
	 * @see launch
	 * @see WvWindowFactory.id
	 */
	@MainThread
	fun launch(windowFactoryId: String, config: (Intent.() -> Unit)?): WvWindowHandle {
		assertThreadMain()
		ensureOpen()

		val child = WvWindowHandleImpl(this)
		open(child)

		val app = CoreApplication.get()
		Intent(app, WvWindowActivity::class.java)

		val intent = child.newLaunchIntent(app, windowFactoryId)
		config?.invoke(intent)
		app.startActivity(intent)

		return child
	}

	@MainThread
	actual override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		assertThreadMain()
		if (isOpen) {
			val app = CoreApplication.get()
			val intent = newPostIntent(app, bus, value)
			app.startActivity(intent)
			return true
		}
		return false
	}

	// --

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

	actual companion object {
		private const val URI_SCHEME_to_ID_HEX = "x:" // The 'x' stands for "hexadecimal"

		private const val EXTRAS_KEY_to_ID = "id"
		private const val EXTRAS_KEY_to_PARENT_ID = "parent"
		private const val EXTRAS_KEY_to_WINDOW_FACTORY_ID = "factory"

		private const val EXTRAS_KEY_to_POST_BUS_ID = "postId"
		private const val EXTRAS_KEY_to_POST_PAYLOAD = "payload"

		private val globalRoot_ = WvWindowHandleImpl(null)

		init {
			if (ASSERTIONS_ENABLED) open(globalRoot_)
		}

		val globalRoot
			get() = globalRoot_.apply {
				if (isClosed) {
					if (ASSERTIONS_ENABLED) errorAssertion("Unexpected: something closed the global root handle.")
					open(this)
				}
			}

		/**
		 * @see WvWindowHandleImpl.get
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun getId(intent: Intent): Int =
			intent.getIntExtra(EXTRAS_KEY_to_ID, INVALID_ID)

		/**
		 * @see WvWindowHandleImpl.getId
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun getParentId(intent: Intent): Int =
			intent.getIntExtra(EXTRAS_KEY_to_PARENT_ID, INVALID_ID)

		/**
		 * @see WvWindowHandleImpl.newLaunchIntent
		 * @see WvWindowHandleImpl.getWindowFactory
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun getWindowFactoryId(intent: Intent): String? =
			intent.getStringExtra(EXTRAS_KEY_to_WINDOW_FACTORY_ID)

		/**
		 * @see WvWindowHandleImpl.newLaunchIntent
		 * @see WvWindowHandleImpl.getWindowFactoryId
		 */
		@Suppress("NOTHING_TO_INLINE")
		@MainThread
		inline fun getWindowFactory(intent: Intent) = getWindowFactoryId(intent)?.let {
			WvWindowFactory.get(it) // Presumably requires the main thread
		}

		/**
		 * @see WvWindowHandleImpl.newLaunchIntent
		 * @see WvWindowHandleImpl.getId
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun get(intent: Intent): WvWindowHandleImpl? = get(getId(intent))

		/**
		 * @see WvWindowHandleImpl.newPostIntent
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun getPostBusId(intent: Intent): String? =
			intent.getStringExtra(EXTRAS_KEY_to_POST_BUS_ID)

		/**
		 * @see WvWindowHandleImpl.newPostIntent
		 */
		@Suppress("NOTHING_TO_INLINE")
		@AnyThread
		inline fun getPostPayload(intent: Intent): SerializationEncoded? =
			SerializationEncoded.getFrom(intent, EXTRAS_KEY_to_POST_PAYLOAD)

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
