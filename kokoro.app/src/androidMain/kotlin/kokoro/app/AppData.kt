package kokoro.app

import kokoro.app.AppData_common.ensureCacheMain
import okio.FileSystem
import okio.Path.Companion.toOkioPath

actual object AppData {

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	@JvmField actual val cacheMain = ensureCacheMain(
		MainApplication.get().cacheDir // Assumed thread-safe
			.toOkioPath(), FileSystem.SYSTEM
	)
}
