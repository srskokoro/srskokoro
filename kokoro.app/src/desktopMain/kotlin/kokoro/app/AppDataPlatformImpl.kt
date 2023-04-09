package kokoro.app

import kokoro.app.AppDataOverrides.Companion.get as getOverrides
import net.harawata.appdirs.AppDirsFactory.getInstance as getAppDirsFactory

@Suppress("NOTHING_TO_INLINE")
actual object AppDataPlatformImpl {

	@JvmStatic actual fun forDefaultRoot(): String =
		getOverrides().defaultRoot ?: getPlatformAppDir()

	@JvmStatic actual fun forRoamingRoot(): String =
		getOverrides().run { roamingRoot ?: defaultRoot } ?: getPlatformAppDir(roaming = true)

	@JvmStatic actual fun forLocalRoot(): String =
		getOverrides().run { localRoot ?: defaultRoot } ?: getPlatformAppDir()

	@JvmStatic actual fun forCacheRoot(): String =
		getOverrides().run { cacheRoot ?: defaultRoot } ?: getPlatformAppDir()

	// --

	private const val APP_DATA_DIR_NAME = "SRSKokoro"

	private fun getPlatformAppDir(roaming: Boolean = false) = getAppDirsFactory()
		.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ roaming)
}
