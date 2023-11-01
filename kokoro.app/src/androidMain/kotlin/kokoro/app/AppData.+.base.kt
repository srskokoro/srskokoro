package kokoro.app

import okio.Path
import okio.Path.Companion.toPath

actual fun AppData.findCollectionsDirs(): List<Path> {
	val r = mutableListOf<Path>()
	val dirs = MainApplication.get().getExternalFilesDirs(null)
	if (dirs != null) for (d in dirs) if (d != null) {
		r.add("${d.path}/cols".toPath())
	}
	return r
}
