package kokoro.app

import kokoro.app.AppData_common.ensureCacheMain
import net.harawata.appdirs.AppDirsFactory
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

actual object AppData {

	private const val APP_CACHE_DIR_ROOT_NAME = "SRSKokoroCache"

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	@JvmField actual val cacheMain: Path = ensureCacheMain(
		AppDirsFactory.getInstance().getUserDataDir(
			APP_CACHE_DIR_ROOT_NAME, null, null,
			/* roaming = */ false
		).toPath(), FileSystem.SYSTEM
	)
}
