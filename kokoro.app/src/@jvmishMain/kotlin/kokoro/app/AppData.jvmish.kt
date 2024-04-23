package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import okio.Path
import okio.Path.Companion.toOkioPath

@Suppress("UnusedReceiverParameter")
val AppData.Jvm inline get() = AppDataOnJvm

// --

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi @JvmField internal actual val `-AppData-mainDir`: Path = AppDataOnJvm.mainDir.toOkioPath(false)

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi @JvmField internal actual val `-AppData-cacheDir`: Path = AppDataOnJvm.cacheDir.toOkioPath(false)
