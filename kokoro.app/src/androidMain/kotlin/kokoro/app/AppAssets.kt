package kokoro.app

import okio.Source
import okio.source
import java.io.InputStream

actual object AppAssets

// NOTE: If necessary, make this `volatile` and update on every application
// configuration change -- see, `Application.onConfigurationChanged()`
private val assets = MainApplication.get().assets

actual fun AppAssets.open(path: String): Source = assets.open(path).source()

actual fun AppAssets.openStream(path: String): InputStream = assets.open(path)
