package kokoro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kokoro.app.ui.DummyWindow
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle
import kokoro.app.ui.engine.window.WvWindowId
import kokoro.internal.annotation.MainThread

class MainActivity : ComponentActivity() {

	companion object {
		@MainThread
		private fun loadSpecializedHandle(): WvWindowHandle {
			val fid = WvWindowFactory.id<DummyWindow>() // TODO!
			val id = WvWindowId("MAIN", fid)
			return WvWindowHandle.globalRoot.load(id)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		// See, https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
		// - See also https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen
		installSplashScreen()
		super.onCreate(savedInstanceState)
		loadSpecializedHandle().launch()
		finishAndRemoveTask()
	}
}
