package kokoro.app

import okio.Path.Companion.toOkioPath
import java.io.File

internal actual fun AppData_platformInit() {
	AppData_init(File(CoreApplication.get().filesDir, "main").toOkioPath())
}
