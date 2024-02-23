package kokoro.app.ui.engine.window

import kokoro.internal.DEPRECATION_ERROR
import kokoro.internal.SPECIAL_USE_DEPRECATION

actual typealias WvWindowHandleAttachment = Any

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
internal fun WvWindowHandle.attach(attachment: WvWindowHandleAttachment) {
	@Suppress(DEPRECATION_ERROR)
	this.attachment = attachment
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
internal actual fun WvWindowHandle_destroy_attachment(attachment: WvWindowHandleAttachment) {
	TODO("Not yet implemented")
}
