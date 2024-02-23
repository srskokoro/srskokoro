package kokoro.app.ui.engine.window

import kokoro.internal.SPECIAL_USE_DEPRECATION

expect class WvWindowHandleAttachment

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
internal expect fun WvWindowHandle_destroy_attachment(attachment: WvWindowHandleAttachment)
