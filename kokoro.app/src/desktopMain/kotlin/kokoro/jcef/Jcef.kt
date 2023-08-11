package kokoro.jcef

import kokoro.app.App
import kokoro.app.AppData
import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.io.NioPath
import kokoro.internal.io.toNioPath
import me.friwi.jcefmaven.EnumOS
import me.friwi.jcefmaven.EnumPlatform
import me.friwi.jcefmaven.impl.step.init.CefInitializer
import org.cef.CefApp
import org.cef.CefSettings
import java.io.File
import java.util.stream.Stream
import kotlin.io.path.isSameFileAs
import kotlin.jvm.optionals.getOrNull

object Jcef {

	val bundleDir = System.getProperty("jcef.bundle")?.let { File(it) } ?: File(
		System.getenv("APP_HOME") ?: error(ERROR_JCEF_BUNDLE_DIR_NOT_SET),
		"jcef",
	)

	inline val app: CefApp
		get() = @Suppress("DEPRECATION") CefAppSetup.app

	@Deprecated(SPECIAL_USE_DEPRECATION)
	@PublishedApi
	internal object CefAppSetup {
		val app: CefApp

		init {
			if (CefApp.getState() != CefApp.CefAppState.NONE) {
				// Must not let someone else initialize `CefApp`, since we would
				// like to exclusively customize `CefApp` only here.
				error("Someone else initialized `CefApp`")
			}

			val cefSettings = CefSettings().apply {
				// Must be explicitly set to `false` or the entire UI (not just
				// the browser UI) will refuse input, as if frozen --
				// experienced on Windows 10; not sure on other OS though.
				windowless_rendering_enabled = false

				log_severity = CefSettings.LogSeverity.LOGSEVERITY_DISABLE
				// NOTE: Even if logging is disabled, provide a path still, in
				// case JCEF/CEF doesn't honor our request for disabled logging.
				log_file = AppData.cacheMain.toString() + File.separatorChar + "jcef.debug.log"

				// TODO How to handle locale changes while the app is already running?
				locale = App.currentLocale().toLanguageTag()

				// TODO
				//cache_path = ...
				//remote_debugging_port = ...
			}

			// Must use a mutable list here as `CefInitializer.initialize()` may
			// modify it to add its own entries.
			val cefArgs = ArrayList<String>(16)

			this.app = CefInitializer.initialize(bundleDir, cefArgs, cefSettings)

			Runtime.getRuntime().addShutdownHook(object : Thread() {
				override fun run() = onJvmShutdown()
			})
		}

		private fun onJvmShutdown() {
			app.dispose() // Expected to kill all JCEF helpers

			// Kills JCEF helpers that should really not run anymore, to prevent
			// leaking them when our process isn't even running anymore.
			// - See also, https://bugs.openjdk.org/browse/JDK-4770092
			killExtraneousJcefHelpers(ProcessHandle.current().descendants())
		}
	}

	fun killExtraneousJcefHelpers() {
		killExtraneousJcefHelpers(ProcessHandle.allProcesses())
	}

	// CONTRACT: The value here must match that of `ProcessHandle.info().command()`
	private val jcefHelperPath: NioPath = File(bundleDir, when (EnumPlatform.getCurrentPlatform().os!!) {
		EnumOS.WINDOWS -> "jcef_helper.exe"
		EnumOS.MACOSX -> "jcef Helper.app/Contents/MacOS/jcef Helper" // TODO Verify if this is correct
		EnumOS.LINUX -> "jcef_helper" // TODO Verify if this is correct
	}).toNioPath().toAbsolutePath()

	fun killExtraneousJcefHelpers(processes: Stream<ProcessHandle>) {
		val jcefHelperPath = jcefHelperPath
		// TODO-FIXME Verify that our handling for non-Windows OS is correct
		for (p in processes) {
			val command = p.info().command().getOrNull() ?: continue
			try {
				val commandPath = command.toNioPath().toAbsolutePath()
				if (commandPath.isSameFileAs(jcefHelperPath)) {
					p.destroy()
				}
			} catch (ex: Throwable) {
				if (DEBUG) throw ex
			}
		}
	}
}

private const val ERROR_JCEF_BUNDLE_DIR_NOT_SET = """
JCEF bundle location not set.
Must either set system property "jcef.bundle" (pointing to the JCEF install
directory or bundle), or set up environment variable "APP_HOME" (where
"APP_HOME/jcef" is the JCEF install directory).
"""
