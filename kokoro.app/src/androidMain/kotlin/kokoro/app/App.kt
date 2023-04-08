package kokoro.app

import android.app.Application
import kokoro.app.App_common.ensureCacheMain
import okio.FileSystem
import okio.Path.Companion.toOkioPath

actual object App {

	val context: Application inline get() = MainApplication.get()

	// --

	/**
	 * The primary directory for storing cache data. All cache data should
	 * usually go under this directory. This directory is always a subdirectory
	 * of the cache directory root.
	 */
	@JvmField actual val cacheMain = ensureCacheMain(
		context.cacheDir // Assumed thread-safe
			.toOkioPath(), FileSystem.SYSTEM
	)
}
