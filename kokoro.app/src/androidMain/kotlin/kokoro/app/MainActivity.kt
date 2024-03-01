package kokoro.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kokoro.app.ui.DummyWindow
import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle
import kokoro.app.ui.engine.window.WvWindowHandleBasis
import kokoro.internal.annotation.MainThread

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
	@MainThread
	private fun startMainWindow() {
		try {
			val fid = WvWindowFactory.id<DummyWindow>() // TODO!
			WvWindowHandleBasis.globalRoot.create("MAIN", fid)
		} catch (ex: WvWindowHandle.IdConflictException) {
			// Already launched before and currently not closed.
			ex.oldHandle.postOrDiscard(UiBus.NOTHING, null) // Bring to front!
			return // Done. Skip code below.
		}.launch {
			addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)
		}
	}
}
