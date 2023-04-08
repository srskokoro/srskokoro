package kokoro.app

import android.app.Application

class MainApplication : Application() {
	init {
		MainApplicationHolder.instance = this
	}

	companion object {
		fun get(): MainApplication = MainApplicationSingleton.instance
	}
}

private object MainApplicationHolder {
	@JvmField var instance: MainApplication? = null
}

private object MainApplicationSingleton {
	@JvmField val instance: MainApplication =
		MainApplicationHolder.instance
		?: throw UninitializedPropertyAccessException(
			"Main application was not initialized."
		)
}
