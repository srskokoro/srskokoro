package kokoro.app

import android.app.Application

class MainApplication : Application() {
	init {
		instance = this
	}

	companion object {
		private var instance: MainApplication? = null

		fun getOrNull(): MainApplication? = instance

		fun get(): MainApplication = MainApplicationSingleton.instance
	}
}

private object MainApplicationSingleton {
	@JvmField val instance: MainApplication =
		MainApplication.getOrNull() ?: throw UninitializedPropertyAccessException(
			"Main application was not initialized."
		)
}
