package kokoro.app

import java.io.File

const val APP_CACHE_SCHEMA_VERSION = 0

val APP_CACHE_SCHEMA_VERSION_NAME = APP_CACHE_SCHEMA_VERSION.toString().padStart(4, '0')

expect object App {

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	@JvmStatic val cacheData: File
}
