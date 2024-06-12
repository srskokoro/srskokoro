package kokoro.app

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kokoro.app.ui.DummyWindow
import kokoro.app.ui.engine.window.WvWindowActivity
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle
import kokoro.app.ui.engine.window.get

class MainActivity : WvWindowActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		// See, https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
		// - See also https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen
		installSplashScreen()
		super.onCreate(savedInstanceState)
	}

	override fun initHandle(savedInstanceState: Bundle?): WvWindowHandle {
		val id = "MAIN"
		if (savedInstanceState != null) {
			val h = WvWindowHandle.get(id)
			// NOTE: Even if we're returning from a saved state, the following
			// may still be `null` if the process was killed beforehand.
			if (h != null) return h
		}
		val fid = WvWindowFactory.id<DummyWindow>() // TODO!
		return WvWindowHandle.globalRoot.create(id, fid)
	}
}
