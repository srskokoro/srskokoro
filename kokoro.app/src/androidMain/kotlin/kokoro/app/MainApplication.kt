package kokoro.app

import android.app.Application
import kokoro.app.MainApplication.Singleton.instance as mainApplication

class MainApplication : Application() {
	init {
		instance = this
	}

	companion object {
		private var instance: MainApplication? = null

		fun getOrNull(): MainApplication? = instance

		fun get(): MainApplication = mainApplication
	}

	private object Singleton {
		@JvmField val instance: MainApplication =
			getOrNull() ?: throw UninitializedPropertyAccessException(
				"Main application was not initialized."
			)
	}
}
