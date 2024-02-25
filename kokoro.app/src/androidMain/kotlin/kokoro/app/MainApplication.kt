package kokoro.app

import kokoro.app.ui.engine.window.WvWindowHandle_globalInit
import kokoro.internal.DEPRECATION_ERROR
import kokoro.internal.annotation.MainThread

class MainApplication : CoreApplication() {

	@MainThread
	override fun onCreate() {
		super.onCreate()

		@Suppress(DEPRECATION_ERROR)
		WvWindowHandle_globalInit()
	}
}
