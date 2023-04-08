package kokoro.app

import kokoro.app.AppData_common.ensureDirMain
import net.harawata.appdirs.AppDirsFactory
import okio.Path
import okio.Path.Companion.toPath

actual object AppData {

	private const val APP_DATA_DIR_NAME = "SRSKokoro"

	/**
	 * The primary directory for storing local data. All local data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the local directory root.
	 */
	@JvmField actual val localMain: Path = ensureDirMain(
		AppDirsFactory.getInstance().getUserDataDir(
			APP_DATA_DIR_NAME, null, null,
			/* roaming = */ false
		).toPath()
	)

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	@JvmField actual val cacheMain: Path = ensureDirMain(localMain / "cache")
}
