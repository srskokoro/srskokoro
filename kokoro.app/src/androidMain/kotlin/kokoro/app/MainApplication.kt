package kokoro.app

import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle_globalInit
import kokoro.internal.annotation.MainThread
import kokoro.internal.os.SerializationEncoded
import kotlinx.serialization.modules.EmptySerializersModule

class MainApplication : CoreApplication() {

	@MainThread
	override fun onCreate() {
		super.onCreate()

		SerializationEncoded.init(EmptySerializersModule()) // TODO!

		@Suppress("DEPRECATION_ERROR") WvWindowFactory.globalInit()
		@Suppress("DEPRECATION_ERROR") WvWindowHandle_globalInit()
	}
}
