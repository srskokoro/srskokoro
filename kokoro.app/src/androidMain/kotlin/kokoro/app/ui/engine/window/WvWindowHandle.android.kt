package kokoro.app.ui.engine.window

import android.app.ActivityManager.AppTask
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import kokoro.app.CoreApplication
import kokoro.app.ui.engine.UiBus
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.os.SerializationEncoded
import kokoro.internal.os.SerializationEncoded.Companion.getSerializationEncodedExtra
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job

@OptIn(nook::class)
actual class WvWindowHandle @nook internal actual constructor(
	id: WvWindowId?,
	parent: WvWindowManager?,
) : WvWindowManager(id, parent) {

	@JvmField @nook var activity_: WvWindowActivity? = null
	@JvmField @nook var taskId_: Int = 0
	@JvmField @nook var task_: AppTask? = null

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	@nook inline fun detachPeer() {
		activity_ = null
		taskId_ = 0
		task_ = null
	}

	// --

	@JvmField @nook var closeJob_: CompletableJob = Job()
	actual override val closeJob: Job get() = closeJob_

	@MainThread
	actual override fun onClose() {
		closeJob_.complete()

		kotlin.run {
			task_?.let {
				activity_ = null
				taskId_ = 0
				task_ = null

				it.finishAndRemoveTask()
				return@run // Skip code below
			}
			activity_?.let {
				activity_ = null
				taskId_ = 0

				it.finishAndRemoveTask()
				return@run // Skip code below
			}
			val taskId = taskId_
			taskId_ = 0
			// NOTE: It seems that Android allows a zero task ID, but we don't
			// care. Android Q and above probably even allows negative task IDs.
			if (taskId > 0) CoreApplication.finishAndRemoveTask(taskId)
		}
	}

	actual companion object {

		@AnyThread
		actual fun closed(): WvWindowHandle = WvWindowHandle(null, null)

		// --

		/** @see kokoro.app.ui.engine.window.WvWindowHandle.newLaunchIntent */
		@nook const val ACTION_LAUNCH = "kokoro.app.ui.engine.window.action.LAUNCH"
		@nook const val EXTRAS_KEY_to_PARENT_ID = "parent"

		/** @see kokoro.app.ui.engine.window.WvWindowHandle.newPostIntent */
		@nook const val ACTION_POST = "kokoro.app.ui.engine.window.action.POST"
		@nook const val EXTRAS_KEY_to_POST_BUS_ID = "postId"
		@nook const val EXTRAS_KEY_to_POST_PAYLOAD = "payload"

		/** WARNING: Must only be modified from the main thread. */
		private var globalRoot_: WvWindowHandle? = null

		val globalRoot: WvWindowHandle
			@MainThread get() {
				assertThreadMain()
				var r = globalRoot_
				if (r != null) return r
				r = load(WvWindowId("GLOBAL", WvWindowFactoryId.NOTHING), null)
				globalRoot_ = r
				return r
			}

		@Suppress("NOTHING_TO_INLINE")
		inline fun get(intent: Intent): WvWindowHandle? =
			intent.data?.let { get(WvWindowId(it)) }

		@Suppress("NOTHING_TO_INLINE")
		inline fun getParent(intent: Intent): Uri? =
			IntentCompat.getParcelableExtra(intent, EXTRAS_KEY_to_PARENT_ID, Uri::class.java)

		@Suppress("NOTHING_TO_INLINE")
		inline fun getPostBusId(intent: Intent): String? =
			intent.getStringExtra(EXTRAS_KEY_to_POST_BUS_ID)

		@Suppress("NOTHING_TO_INLINE")
		inline fun getPostPayload(intent: Intent): SerializationEncoded? =
			intent.getSerializationEncodedExtra(EXTRAS_KEY_to_POST_PAYLOAD)
	}

	@MainThread
	fun newLaunchIntent(app: Application): Intent? {
		assertThreadMain()

		val id = id ?: return null
		id.checkOnLaunch()

		return Intent(ACTION_LAUNCH, id.toUri(), app, WvWindowActivity::class.java).apply {
			parent?.let { p -> putExtra(EXTRAS_KEY_to_PARENT_ID, p.id?.toUri()) }

			// Necessary or `Application.startActivity()` will fail
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
	}

	@MainThread
	fun <T> newPostIntent(app: Application, bus: UiBus<T>, payload: T): Intent? {
		assertThreadMain()

		val id = id ?: return null
		return Intent(ACTION_POST, id.toUri(), app, WvWindowActivity::class.java).apply {
			putExtra(EXTRAS_KEY_to_POST_BUS_ID, bus.id)

			val enc = SerializationEncoded(payload, bus.serialization) // May throw
			putExtra(EXTRAS_KEY_to_POST_PAYLOAD, enc)

			// Necessary or `Application.startActivity()` will fail
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
	}

	@MainThread
	inline fun launchOrReject(config: Intent.() -> Unit): Boolean {
		val app = CoreApplication.get()
		app.startActivity((newLaunchIntent(app) ?: return false).apply(config))
		return true
	}

	@MainThread
	actual override fun launchOrReject(): Boolean {
		return launchOrReject {}
	}

	@MainThread
	actual override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		val app = CoreApplication.get()
		app.startActivity(newPostIntent(app, bus, value) ?: return false)
		return true
	}
}

// --

@Suppress("NOTHING_TO_INLINE")
inline fun WvWindowId.toUri(): Uri = Uri.fromParts("x", name, factoryId.id)

@Suppress("NOTHING_TO_INLINE")
inline fun WvWindowId(uri: Uri) = WvWindowId(
	uri.schemeSpecificPart,
	uri.fragment.let {
		if (it != null) WvWindowFactoryId.wrap(it)
		else WvWindowFactoryId.NOTHING
	},
)
