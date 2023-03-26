package kokoro.app

import android.app.Application
import java.io.File

actual object App {

	@JvmField val context: Application =
		MainApplication.`$$inst` ?: throw UninitializedPropertyAccessException("Main application not yet initialized")

	// --

	private var _cacheMain: File? = null

	@JvmStatic
	actual val cacheMain: File
		get() = _cacheMain ?: File(
			context.cacheDir, // Assumed thread-safe
			APP_CACHE_SCHEMA_VERSION_NAME
		).also {
			it.mkdir() // TODO Fail fast!
			_cacheMain = it
		}
}
