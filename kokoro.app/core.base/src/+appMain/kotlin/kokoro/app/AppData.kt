package kokoro.app

import kokoro.internal.DEBUG
import kokoro.internal.io.SYSTEM
import kokoro.internal.io.ensureDirs
import okio.FileSystem
import okio.IOException
import okio.Path
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * WARNING: All access to this class will fail unless [AppData_init]`()` was
 * called "beforehand" – that is, this must be done before the "first" access to
 * this class, or there's no way to recover from the failure other than
 * restarting the executable's process.
 */
object AppData {

	/**
	 * The primary directory for storing local app data.
	 *
	 * A file under this directory may be device-bound, that is, it might be
	 * expected to never be transferred to other devices, e.g., device
	 * identifiers meant to uniquely identify the device – see also,
	 * “[Best practices for unique identifiers | Android Developers](https://developer.android.com/training/articles/user-data-ids)”
	 *
	 * NOTE: The [Path] value here is [canonical][FileSystem.canonicalize] (and
	 * absolute).
	 */
	@JvmField val mainDir: Path = AppData_init_helper.mainDir
		?: throw Error("Function `${::AppData_init.name}()` has not been called")

	@JvmStatic val mainLogsDir: Path get() = _mainLogsDir.value

	private object _mainLogsDir {
		@JvmField val value = (mainDir / "logs").ensureDirs()
	}
}

private object AppData_init_helper {
	@JvmField var mainDir: Path? = null
}

/**
 * NOTE: Call this prior to using [AppData].
 *
 * @param mainDir A path to an already "existing" directory.
 *
 * @throws IOException if [mainDir] cannot be resolved. This will occur if the
 *   path doesn't exist, if the current working directory doesn't exist or is
 *   inaccessible, or if another failure occurs while resolving the path.
 */
@Throws(IOException::class)
fun AppData_init(mainDir: Path) {
	if (DEBUG && AppData_init_helper.mainDir != null) {
		throw Error("Function `${::AppData_init.name}()` should only be called once")
	}
	AppData_init_helper.mainDir = FileSystem.SYSTEM.canonicalize(mainDir) // Throws on non-existing path!
}
