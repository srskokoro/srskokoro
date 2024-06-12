package kokoro.app

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kokoro.app.ui.DummyWindow
import kokoro.app.ui.engine.window.WvWindowActivity
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.app.ui.engine.window.WvWindowHandle
import kokoro.app.ui.engine.window.get
import kokoro.internal.annotation.MainThread

class MainActivity : WvWindowActivity() {

	companion object {

		@JvmField val COMPONENT_CLASS_NAME: String = MainActivity::class.java.name

		@MainThread
		fun loadSpecializedHandle(): WvWindowHandle {
			val id = "MAIN"
			WvWindowHandle.get(id)?.let { return it }

			val fid = WvWindowFactory.id<DummyWindow>() // TODO!
			return WvWindowHandle.globalRoot.create(id, fid)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		// See, https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
		// - See also https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen
		installSplashScreen()
		super.onCreate(savedInstanceState)
	}
}
