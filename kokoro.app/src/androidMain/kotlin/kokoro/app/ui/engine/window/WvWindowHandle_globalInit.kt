package kokoro.app.ui.engine.window

import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import androidx.collection.MutableScatterMap
import androidx.core.content.getSystemService
import kokoro.app.CoreApplication
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.assertUnreachable

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@MainThread
internal fun WvWindowHandle_globalInit() {
	assertThreadMain()

	val activityManager = CoreApplication.get().getSystemService<ActivityManager>()
	if (activityManager == null) {
		assertUnreachable(or = { "`ActivityManager` seems unsupported" })
		return
	}

	WvWindowHandle_globalRestore(activityManager.appTasks)
		.resolve()
}

@OptIn(nook::class)
@MainThread
private class WvWindowHandle_globalRestore(tasks: List<AppTask>) {
	private val entries = MutableScatterMap<String, Entry>().also { entries ->
		tasks.forEach { task ->
			val intent = task.taskInfo.baseIntent
			if (!WvWindowActivity.shouldHandle(intent)) return@forEach

			val id = WvWindowHandle.getId(intent)
			run<Unit> {
				if (id == null) return@run // Invalid request.

				val fid = WvWindowHandle.getWindowFactoryIdStr(intent)
					?: return@run // Not a window display request.

				// NOTE: Parent ID is `null` when there should be no parent.
				val parentId = WvWindowHandle.getParentId(intent)

				entries[id] = Entry(
					id,
					fid = fid,
					parentId = parentId,
					task,
				)
				return@forEach // Done. Skip code below.
			}

			// The task with an invalid request should be removed.
			task.finishAndRemoveTask()
		}
	}

	private class Entry(
		@JvmField val id: String,
		@JvmField val fid: String,
		@JvmField val parentId: String?,
		@JvmField val task: AppTask,
	) {
		@JvmField var visited = false
		@JvmField var handle: WvWindowHandle? = null
	}

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
		val p = if (parentId == null) null // Parent was purposely not set.
		else entries[parentId]?.resolve() ?: return null // Resolution failed.

		return WvWindowHandle.create(
			id = id,
			WvWindowFactoryId.wrap(fid),
			parent = p,
		).also { h ->
			handle = h
			h.attachPlatformContext(task)
		}
	}
}
