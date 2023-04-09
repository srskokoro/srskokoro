package kokoro.app

import net.harawata.appdirs.AppDirsFactory

@Suppress("NOTHING_TO_INLINE")
actual object AppDataPlatformDefaults {

	private const val APP_DATA_DIR_NAME = "SRSKokoro"

	@JvmStatic actual inline fun forDefaultRoot() = forLocalRoot()

	@JvmStatic actual fun forRoamingRoot(): String = AppDirsFactory.getInstance()
		.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ true)

	@JvmStatic actual fun forLocalRoot(): String = AppDirsFactory.getInstance()
		.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ false)

	@JvmStatic actual inline fun forCacheRoot() = forLocalRoot()
}
