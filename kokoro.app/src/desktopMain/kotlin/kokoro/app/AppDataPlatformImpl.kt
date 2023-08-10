package kokoro.app

import kokoro.app.AppBuildDesktop.APP_DATA_DIR_NAME
import java.io.File
import kokoro.app.AppDataOverrides.Companion.get as getOverrides
import net.harawata.appdirs.AppDirsFactory.getInstance as getAppDirsFactory

actual object AppDataPlatformImpl {

	@JvmStatic actual fun forRoamingRoot(): String = getAbsolutePath(
		getOverrides().run { roamingRoot ?: defaultRoot } ?: getPlatformAppDir(roaming = true))

	@JvmStatic actual fun forLocalRoot(): String = getAbsolutePath(
		getOverrides().run { localRoot ?: defaultRoot } ?: getPlatformAppDir())

	@JvmStatic actual fun forDeviceBoundRoot(): String = getAbsolutePath(
		getOverrides().defaultRoot ?: getPlatformAppDir())

	@JvmStatic actual fun forCacheRoot(): String = getAbsolutePath(
		getOverrides().run { cacheRoot ?: defaultRoot } ?: getAppDirsFactory()
			.getUserCacheDir(APP_DATA_DIR_NAME, null, null))
}

@Suppress("NOTHING_TO_INLINE")
private inline fun getPlatformAppDir(roaming: Boolean = false) = getAppDirsFactory()
	.getUserDataDir(APP_DATA_DIR_NAME, null, null, /* roaming = */ roaming)

@Suppress("NOTHING_TO_INLINE")
private inline fun getAbsolutePath(path: String) =
	// Canonical, normalized & absolute -- see also, https://stackoverflow.com/a/53950275
	File(path).canonicalPath
