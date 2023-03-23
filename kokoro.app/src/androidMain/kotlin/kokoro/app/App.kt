package kokoro.app

import android.app.Application

object App {
	@JvmField val context: Application =
		MainApplication.`$$inst` ?: throw UninitializedPropertyAccessException("Main application not yet initialized")
}
