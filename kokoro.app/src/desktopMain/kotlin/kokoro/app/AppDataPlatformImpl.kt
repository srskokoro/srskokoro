package kokoro.app

import kokoro.app.AppDataOverrides.cacheRoot
import kokoro.app.AppDataOverrides.defaultRoot
import kokoro.app.AppDataOverrides.localRoot
import kokoro.app.AppDataOverrides.roamingRoot
import net.harawata.appdirs.AppDirsFactory.getInstance as getAppDirsFactory

@Suppress("NOTHING_TO_INLINE")
actual object AppDataPlatformImpl {

	private const val APP_DATA_DIR_NAME = "SRSKokoro"

	@JvmStatic actual fun forDefaultRoot(): String = defaultRoot ?: getAppDirsFactory()
		.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ false)

	@JvmStatic actual fun forRoamingRoot(): String = roamingRoot ?: defaultRoot ?: getAppDirsFactory()
		.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ true)

	@JvmStatic actual fun forLocalRoot(): String = localRoot ?: forDefaultRoot()

	@JvmStatic actual fun forCacheRoot(): String = cacheRoot ?: forDefaultRoot()
}
