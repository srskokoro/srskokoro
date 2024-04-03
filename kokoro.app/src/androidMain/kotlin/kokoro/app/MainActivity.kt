package kokoro.app

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kokoro.app.ui.DummyWindow
import kokoro.app.ui.engine.window.WvWindowActivity
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle

class MainActivity : WvWindowActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		// See, https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
		// - See also https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen
		installSplashScreen()
		super.onCreate(savedInstanceState)
	}

	override fun initHandle(): WvWindowHandle {
		val fid = WvWindowFactory.id<DummyWindow>() // TODO!
		return WvWindowHandle.globalRoot.create("MAIN", fid)
	}
}
