package kokoro.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kokoro.app.ui.DummyWindow
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandleImpl

class MainActivity : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		// See, https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
		// - See also https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen
		val splashScreen = installSplashScreen()

		super.onCreate(savedInstanceState)

		// Keep the splash screen visible for this Activity.
		// - https://developer.android.com/develop/ui/views/launch/splash-screen/migrate#prevent
		splashScreen.setKeepOnScreenCondition { true }

		startMainWindow()
		finishAndRemoveTask() // Done!
	}

	/**
	 * This should start the "real" main activity in a separate task entry.
	 */
	private fun startMainWindow() {
		val windowFactoryId = WvWindowFactory.id<DummyWindow>() // TODO!
		WvWindowHandleImpl.globalRoot.launch(windowFactoryId) {
			addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)
		}
	}
}
