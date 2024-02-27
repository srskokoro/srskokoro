package kokoro.app

import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle_globalInit
import kokoro.internal.annotation.MainThread

class MainApplication : CoreApplication() {

	@MainThread
	override fun onCreate() {
		super.onCreate()

		@Suppress("DEPRECATION_ERROR") WvWindowHandle_globalInit()
		@Suppress("DEPRECATION_ERROR") WvWindowFactory.globalInit()
	}
}
