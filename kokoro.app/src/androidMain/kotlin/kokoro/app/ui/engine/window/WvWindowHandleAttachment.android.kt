package kokoro.app.ui.engine.window

import android.app.Activity
import android.app.ActivityManager
import kokoro.internal.DEPRECATION_ERROR
import kokoro.internal.SPECIAL_USE_DEPRECATION

actual typealias WvWindowHandleAttachment = Any

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
internal fun WvWindowHandle.attach(activity: WvWindowActivity) {
	@Suppress(DEPRECATION_ERROR)
	attachment = activity
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
internal fun WvWindowHandle.attach(task: ActivityManager.AppTask) {
	@Suppress(DEPRECATION_ERROR)
	attachment = task
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
internal actual fun WvWindowHandle_destroy_attachment(attachment: WvWindowHandleAttachment) {
	when (attachment) {
		is Activity -> attachment.finishAndRemoveTask()
		is ActivityManager.AppTask -> attachment.finishAndRemoveTask()
		else -> throw AssertionError("Unexpected: $attachment")
	}
}
