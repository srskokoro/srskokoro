package kokoro.app

import android.app.Application
import java.io.File

actual object App {

	@JvmField val context: Application =
		MainApplication.`$$inst` ?: throw UninitializedPropertyAccessException("Main application not yet initialized")

	// --

	private var _localDir: File? = null
	@JvmStatic actual val localDir: File
		get() = _localDir ?: context.noBackupFilesDir // Assumed thread-safe
			.also { _localDir = it }
}
