package kokoro.app

import android.content.res.AssetManager
import okio.Source
import okio.source
import java.io.InputStream

actual object AppAssets {
	// NOTE: If necessary, make this `volatile` and update on every application
	// configuration change -- see, `Application.onConfigurationChanged()`
	@JvmField val manager: AssetManager = CoreApplication.get().assets
}

actual fun AppAssets.open(path: String): Source = manager.open(path).source()

actual fun AppAssets.openStream(path: String): InputStream = manager.open(path)
