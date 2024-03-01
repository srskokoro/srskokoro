package kokoro.app.ui.engine.window

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Intent
import android.net.Uri
import kokoro.app.CoreApplication
import kokoro.app.ui.engine.UiBus
import kokoro.internal.DEBUG
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.os.SerializationEncoded

@OptIn(nook::class)
actual sealed class WvWindowHandleBasis @AnyThread actual constructor(
	id: String?,
	actual val windowFactoryId: WvWindowFactoryId,
	parent: WvWindowHandle?,
) {
	/** WARNING: Must only be modified from the main thread. */
	@JvmField @nook var uri_ =
		if (id == null) null else Uri.fromParts("x", id, null)

	actual val id: String?
		inline get() = uri_?.let { getId(it) }

	/** WARNING: Must only be modified from the main thread. */
	@JvmField @nook actual var parent_: WvWindowHandle? = parent

	// --

	/** WARNING: Must only be modified from the main thread. */
	@JvmField @nook var context_: Any? = null

	val activity inline get() = context_ as? WvWindowActivity

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	inline fun attachContext(activity: WvWindowActivity) {
		this.context_ = activity
	}

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	inline fun attachContext(task: ActivityManager.AppTask) {
		this.context_ = task
	}

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	inline fun detachContext() {
		this.context_ = null
	}

	// --

	@MainThread
	fun newLaunchIntent(app: Application): Intent {
		assertThreadMain()

		val uri = uri_ ?: throw WvWindowHandle.E_Closed()
		if (DEBUG) windowFactoryId.let { fid ->
			if (fid.isNothing || WvWindowFactory.get(fid) == null) error(
				"Window factory ID cannot be used to launch windows: $fid"
			)
		}

		return Intent(app, WvWindowActivity::class.java).apply {
			// Necessary or `Application.startActivity()` will fail
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

			data = uri
			parent_?.let { p -> putExtra(EXTRAS_KEY_to_PARENT_ID, p.id) }
			putExtra(EXTRAS_KEY_to_WINDOW_FACTORY_ID, windowFactoryId.id)
		}
	}

	@MainThread
	fun <T> newPostIntent(app: Application, bus: UiBus<T>, payload: T): Intent? {
		assertThreadMain()

		val uri = uri_ ?: return null
		return Intent(app, WvWindowActivity::class.java).apply {
			// Necessary or `Application.startActivity()` will fail
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

			data = uri
			putExtra(EXTRAS_KEY_to_POST_BUS_ID, bus.id)
			SerializationEncoded(payload, bus.serialization)
				.putInto(this, EXTRAS_KEY_to_POST_PAYLOAD)
		}
	}

	// --

	@MainThread
	inline fun launch(config: Intent.() -> Unit) {
		val app = CoreApplication.get()
		app.startActivity(newLaunchIntent(app).apply(config))
	}

	@MainThread
	actual fun launch() {
		launch {}
	}

	@MainThread
	actual fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		val app = CoreApplication.get()
		app.startActivity(newPostIntent(app, bus, value) ?: return false)
		return true
	}

	@MainThread
	protected actual fun onClose() {
		uri_ = null // Marks as closed
		when (val c = context_) {
			null -> return // Skip code below
			is Activity -> c.finishAndRemoveTask()
			is ActivityManager.AppTask -> c.finishAndRemoveTask()
			else -> throw AssertionError("Unexpected: $c")
		}
		detachContext()
	}

	actual val isClosed: Boolean inline get() = uri_ == null

	companion object {
		@nook const val EXTRAS_KEY_to_PARENT_ID = "parent"
		@nook const val EXTRAS_KEY_to_WINDOW_FACTORY_ID = "factory"

		@nook const val EXTRAS_KEY_to_POST_BUS_ID = "postId"
		@nook const val EXTRAS_KEY_to_POST_PAYLOAD = "payload"

		/** WARNING: Must only be modified from the main thread. */
		private var globalRoot_: WvWindowHandle? = null

		val globalRoot: WvWindowHandle
			@MainThread get() {
				var r = globalRoot_
				if (r != null) return r

				assertThreadMain()

				r = WvWindowHandle.create(
					id = "GLOBAL",
					WvWindowFactoryId.NOTHING,
					parent = null,
				)
				globalRoot_ = r
				return r
			}

		@Suppress("NOTHING_TO_INLINE")
		@nook inline fun getId(uri: Uri): String? = uri.schemeSpecificPart

		@Suppress("NOTHING_TO_INLINE")
		inline fun getId(intent: Intent): String? =
			intent.data?.let { getId(it) }

		@Suppress("NOTHING_TO_INLINE")
		inline fun getParentId(intent: Intent): String? =
			intent.getStringExtra(EXTRAS_KEY_to_PARENT_ID)

		@Suppress("NOTHING_TO_INLINE")
		inline fun getWindowFactoryIdStr(intent: Intent): String? =
			intent.getStringExtra(EXTRAS_KEY_to_WINDOW_FACTORY_ID)

		@Suppress("NOTHING_TO_INLINE")
		inline fun get(intent: Intent): WvWindowHandle? =
			getId(intent)?.let { WvWindowHandle.get(it) }

		@Suppress("NOTHING_TO_INLINE")
		inline fun getPostBusId(intent: Intent): String? =
			intent.getStringExtra(EXTRAS_KEY_to_POST_BUS_ID)

		@Suppress("NOTHING_TO_INLINE")
		inline fun getPostPayload(intent: Intent): SerializationEncoded? =
			SerializationEncoded.getFrom(intent, EXTRAS_KEY_to_POST_PAYLOAD)
	}
}
