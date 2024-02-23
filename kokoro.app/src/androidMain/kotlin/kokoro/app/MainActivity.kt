package kokoro.app

import android.app.Activity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

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
		finish() // Done!
	}

	private fun startMainWindow() {
		// TODO Implement!
	}
}
