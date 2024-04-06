package kokoro.app.ui.swing

import com.formdev.flatlaf.FlatSystemProperties
import com.formdev.flatlaf.ui.FlatNativeLibrary_isInitialized
import kokoro.internal.check
import kotlinx.atomicfu.atomic
import main.appHomeDir
import java.io.File

object AppLafNatives {
	private val init = atomic(false)

	fun init() {
		if (!init.compareAndSet(false, true)) return // Already set up

		check(!FlatNativeLibrary_isInitialized, or = {
			"Cannot change FlatLaf native library path: someone else already initialized it."
		})

		System.setProperty(
			FlatSystemProperties.NATIVE_LIBRARY_PATH,
			File(appHomeDir, "flatlaf").path,
		)
	}
}
