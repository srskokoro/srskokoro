package kokoro.internal.os

import android.app.ActivityManager
import android.os.Build

val ActivityManager.RecentTaskInfo.taskIdCompat: Int
	inline get() =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) taskId
		else @Suppress("DEPRECATION") persistentId
