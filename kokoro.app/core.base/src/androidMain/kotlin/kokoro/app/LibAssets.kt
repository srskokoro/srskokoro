package kokoro.app

import android.content.res.AssetManager
import java.io.InputStream

actual object LibAssets {
	// NOTE: If necessary, make this `volatile` and update on every application
	// configuration change -- see, `Application.onConfigurationChanged()`
	@JvmField val manager: AssetManager = CoreApplication.get().assets
}

actual fun LibAssets.openStream(path: String): InputStream = manager.open(path)
