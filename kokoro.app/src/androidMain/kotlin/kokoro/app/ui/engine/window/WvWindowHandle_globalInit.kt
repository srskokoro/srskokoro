package kokoro.app.ui.engine.window

import android.app.ActivityManager.AppTask
import androidx.collection.MutableScatterMap
import kokoro.app.CoreApplication
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@MainThread
internal fun WvWindowHandle_globalInit() {
	assertThreadMain()

	val am = CoreApplication.activityManager

	WvWindowHandle.globalRoot // Force init
	WvWindowHandle_globalRestore(am.appTasks)
		.resolve()
}

@OptIn(nook::class)
@MainThread
private class WvWindowHandle_globalRestore(tasks: List<AppTask>) {
	private val entries = MutableScatterMap<WvWindowId, Entry>().also { entries ->
		tasks.forEach { task ->
			val intent = task.taskInfo.baseIntent
			if (intent.action != WvWindowHandle.ACTION_LAUNCH) return@forEach

			val uri = intent.data
			run<Unit> {
				if (uri == null) return@run // Invalid request.

				val id = WvWindowId(uri)

				// NOTE: Parent is `null` when there should be no parent.
				val parentId = WvWindowHandle.getParent(intent)?.let { WvWindowId(it) }

				entries[id] = Entry(id, parentId = parentId, task)
				return@forEach // Done. Skip code below.
			}

			// The task with an invalid request should be removed.
			task.finishAndRemoveTask()
		}
	}

	private class Entry(
		@JvmField val id: WvWindowId,
		@JvmField val parentId: WvWindowId?,
		@JvmField val task: AppTask,
	) {
		@JvmField var visited = false
		@JvmField var handle: WvWindowHandle? = null
	}

	@MainThread
	fun resolve() {
		entries.forEachValue { it.resolve() }
	}

	/**
	 * @return the [WvWindowHandle] for the [Entry] or null (on failure).
	 */
	@MainThread
	private fun Entry.resolve(): WvWindowHandle? {
		// NOTE: The following is set up in such a way that even on circular
		// references, it would still fail gracefully by returning null.

		if (visited) return handle
		visited = true

		val parentId = parentId
		val p = if (parentId != null) {
			val parentEntry = entries[parentId]
			if (parentEntry == null) {
				WvWindowHandle.get(parentId)
			} else {
				parentEntry.resolve()
			} ?: return null // Resolution failed.
		} else null // Parent was purposely not set.

		return WvWindowHandle.load(id, p).also { h ->
			handle = h
			h.task_ = task
		}
	}
}
