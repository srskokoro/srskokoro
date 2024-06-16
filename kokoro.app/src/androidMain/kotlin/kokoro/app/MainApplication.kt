package kokoro.app

import kokoro.app.ui.engine.UiSerialization
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle_globalInit
import kokoro.internal.annotation.MainThread
import kokoro.internal.os.SerializationEncoded

class MainApplication : CoreApplication() {

	@MainThread
	override fun onCreate() {
		super.onCreate()

		SerializationEncoded.init(UiSerialization.module)

		@Suppress("DEPRECATION_ERROR") WvWindowFactory.globalInit()
		@Suppress("DEPRECATION_ERROR") WvWindowHandle_globalInit()
	}
}
