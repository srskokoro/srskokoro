package kokoro.app

import okio.Path

actual object AppData {

	/**
	 * The primary directory for storing local data. All local data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the local directory root.
	 */
	actual val localMain: Path
		get() = TODO("Not yet implemented")

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	actual val cacheMain: Path
		get() = TODO("Not yet implemented")
}
