package kokoro.app

import android.app.Application

class MainApplication : Application() {
	init {
		instance = this
	}

	companion object {
		private var instance: MainApplication? = null

		fun getOrNull(): MainApplication? = instance

		fun get(): MainApplication = Singleton.instance
	}

	private object Singleton {
		@JvmField val instance: MainApplication =
			getOrNull() ?: throw UninitializedPropertyAccessException(
				"Main application was not initialized."
			)
	}
}
