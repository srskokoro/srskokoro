package kokoro.app.ui.engine.window

import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import androidx.collection.MutableIntObjectMap
import androidx.core.content.getSystemService
import kokoro.app.CoreApplication
import kokoro.app.ui.engine.window.WvWindowHandle.Companion.INVALID_ID
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
		assertUnreachable(or_fail_with = { "`ActivityManager` seems unsupported" })
		return
	}

	WvWindowHandle_globalRestore(activityManager.appTasks)
		.resolve()
}

@MainThread
private class WvWindowHandle_globalRestore(tasks: List<AppTask>) {
	private val entries: MutableIntObjectMap<Entry>

	init {
		val entries = MutableIntObjectMap<Entry>()
		for (task in tasks) {
			val intent = task.taskInfo.baseIntent
			if (!WvWindowActivity.shouldHandle(intent)) continue

			val id = WvWindowHandleImpl.getId(intent)
			if (id > INVALID_ID) {
				// NOTE: Parent ID is `INVALID_ID` when purposely not set.
				val parentId = WvWindowHandleImpl.getParentId(intent)
				entries.put(id, Entry(id, parentId))
			} else {
				task.finishAndRemoveTask()
			}
		}
		this.entries = entries
	}

	private class Entry(
		@JvmField val id: Int,
		@JvmField val parentId: Int,
	) {
		@JvmField var visited = false
		@JvmField var handle: WvWindowHandleImpl? = null
	}

	fun resolve() {
		entries.forEachValue { it.resolve() }
	}

	/**
	 * @return the [WvWindowHandleImpl] for the [Entry] or null (on failure).
	 */
	@MainThread
	private fun Entry.resolve(): WvWindowHandleImpl? {
		// NOTE: The following is set up in such a way that even on circular
		// references, it would still fail gracefully by returning null.

		if (visited) return handle
		visited = true

		run<Unit> {
			val parentId = parentId
			if (parentId != INVALID_ID) {
				entries[parentId]?.resolve() ?: return@run // Fail
			} else {
				null
			}.let { p ->
				val h = WvWindowHandleImpl(p)
				if (WvWindowHandle.openAt(id, h)) {
					handle = h
					return h
				}
				// Otherwise, fail.
			}
		}
		return null
	}
}
