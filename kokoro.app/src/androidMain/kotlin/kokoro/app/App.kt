package kokoro.app

import android.app.Application
import java.io.File

actual object App {

	@JvmField val context: Application =
		MainApplication.`$$inst` ?: throw UninitializedPropertyAccessException("Main application not yet initialized")

	// --

	private var _cacheData: File? = null

	@JvmStatic
	actual val cacheData: File
		get() = _cacheData ?: File(
			context.cacheDir, // Assumed thread-safe
			APP_CACHE_SCHEMA_VERSION_NAME
		).also {
			it.mkdir() // TODO Fail fast!
			_cacheData = it
		}
}
