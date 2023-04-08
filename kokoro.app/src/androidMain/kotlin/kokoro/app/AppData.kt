package kokoro.app

import kokoro.app.AppData_common.ensureDirMain
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

actual object AppData {

	/**
	 * The primary directory for storing local data. All local data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the local directory root.
	 */
	@JvmField actual val localMain: Path = ensureDirMain(
		MainApplication.get().filesDir // Assumed thread-safe
			.toOkioPath() / "local", FileSystem.SYSTEM
	)

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	@JvmField actual val cacheMain = ensureDirMain(
		MainApplication.get().cacheDir // Assumed thread-safe
			.toOkioPath(), FileSystem.SYSTEM
	)
}
