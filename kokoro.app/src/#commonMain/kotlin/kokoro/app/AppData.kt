package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import okio.FileSystem
import okio.Path
import kotlin.jvm.JvmField

object AppData

/**
 * The primary directory for storing local app data.
 *
 * A file under this directory may be device-bound, that is, it might be
 * expected to never be transferred to other devices, e.g., device identifiers
 * meant to uniquely identify the device – see also,
 * “[Best practices for unique identifiers | Android Developers](https://developer.android.com/training/articles/user-data-ids)”
 *
 * NOTE: The [Path] value here is [canonical][FileSystem.canonicalize] (and
 * absolute).
 */
@Suppress("UnusedReceiverParameter")
val AppData.mainDir: Path @Suppress("DEPRECATION_ERROR") inline get() = `-AppData-mainDir`

@Suppress("UnusedReceiverParameter")
val AppData.cacheDir: Path @Suppress("DEPRECATION_ERROR") inline get() = `-AppData-cacheDir`

// --

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi @JvmField internal expect val `-AppData-mainDir`: Path

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi @JvmField internal expect val `-AppData-cacheDir`: Path
