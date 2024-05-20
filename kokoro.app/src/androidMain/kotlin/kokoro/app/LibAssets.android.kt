package kokoro.app

import android.content.res.AssetManager
import android.util.LruCache
import kokoro.internal.DEBUG
import okio.FileNotFoundException
import java.io.IOException
import java.io.InputStream

actual object LibAssets {
	// NOTE: If necessary, make this `volatile` and update on every application
	// configuration change -- see, `Application.onConfigurationChanged()`
	@JvmField val manager: AssetManager = CoreApplication.get().assets
}

private val LibAssets_MISSES = object : LruCache<String, Boolean>(32768) {
	override fun sizeOf(key: String, value: Boolean): Int =
		key.length * Char.SIZE_BYTES + /* Estimated object size per entry */ 64
}

actual fun LibAssets.openStreamOrNull(path: String): InputStream? {
	if (LibAssets_MISSES.get(path) == null) try {
		manager.open(path)
	} catch (ex: IOException) {
		if (DEBUG && ex !is FileNotFoundException) throw ex
		LibAssets_MISSES.put(path, true)
	}
	return null
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
