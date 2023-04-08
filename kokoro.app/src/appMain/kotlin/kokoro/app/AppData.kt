package kokoro.app

import okio.Path

const val APP_DATA_SCHEMA_VERSION = 0

val APP_DATA_SCHEMA_VERSION_NAME = APP_DATA_SCHEMA_VERSION.toString().padStart(3, '0')

expect object AppData {

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	val cacheMain: Path
}
