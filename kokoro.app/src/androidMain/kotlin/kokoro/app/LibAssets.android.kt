package kokoro.app

import android.content.res.AssetManager
import okio.FileNotFoundException
import java.io.IOException
import java.io.InputStream

actual object LibAssets {
	// NOTE: If necessary, make this `volatile` and update on every application
	// configuration change -- see, `Application.onConfigurationChanged()`
	@JvmField val manager: AssetManager = CoreApplication.get().assets
}

actual fun LibAssets.openStreamOrNull(path: String): InputStream? = try {
	manager.open(path)
} catch (ex: IOException) {
	null
}

@Throws(FileNotFoundException::class)
actual fun LibAssets.openStream(path: String): InputStream = try {
	manager.open(path)
} catch (ex: IOException) {
	throw E_MissingAsset(ex, path)
}

private fun E_MissingAsset(cause: Throwable, path: String): FileNotFoundException {
	return if (cause is FileNotFoundException) cause
	else FileNotFoundException("Asset file: $path")
		.apply { initCause(cause) }
}
