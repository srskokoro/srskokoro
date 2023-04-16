package kokoro.app

import kokoro.app.AppBuildDesktop.APP_DATA_DIR_NAME
import kokoro.app.AppDataOverrides.Companion.get as getOverrides
import net.harawata.appdirs.AppDirsFactory.getInstance as getAppDirsFactory

actual object AppDataPlatformImpl {

	@JvmStatic actual fun forRoamingRoot(): String =
		getOverrides().run { roamingRoot ?: defaultRoot } ?: getPlatformAppDir(roaming = true)

	@JvmStatic actual fun forLocalRoot(): String =
		getOverrides().run { localRoot ?: defaultRoot } ?: getPlatformAppDir()

	@JvmStatic actual fun forDeviceBoundRoot(): String =
		getOverrides().defaultRoot ?: getPlatformAppDir()

	@JvmStatic actual fun forCacheRoot(): String =
		getOverrides().run { cacheRoot ?: defaultRoot } ?: getAppDirsFactory()
			.getUserCacheDir(APP_DATA_DIR_NAME, null, null)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun getPlatformAppDir(roaming: Boolean = false) = getAppDirsFactory()
	.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ roaming)
