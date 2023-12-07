package kokoro.jcef

import androidx.annotation.GuardedBy
import kokoro.app.AppData
import kokoro.app.i18n.currentLocale
import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.collections.fastForEach
import kokoro.internal.io.NioPath
import kokoro.internal.io.toNioPath
import kokoro.internal.system.CleanProcessExit
import me.friwi.jcefmaven.EnumOS
import me.friwi.jcefmaven.EnumPlatform
import me.friwi.jcefmaven.impl.step.init.CefInitializer
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.CefSettings
import org.cef.callback.CefCommandLine
import org.cef.callback.CefSchemeRegistrar
import org.cef.handler.CefAppHandlerAdapter
import java.io.File
import java.util.stream.Stream
import kotlin.io.path.isSameFileAs
import kotlin.jvm.optionals.getOrNull

object Jcef {

	@JvmField
	val bundleDir = System.getProperty("jcef.bundle")?.let { File(it) } ?: File(
		System.getenv("APP_HOME") ?: throw E_JcefBundleDirNotSet(),
		"jcef",
	)

	@GuardedBy("Jcef")
	private var jcefAppInitialized = false

	private val jcefStateObservers = ArrayList<JcefStateObserver>()

	fun addStateObserver(observer: JcefStateObserver) {
		synchronized(Jcef) {
			if (jcefAppInitialized) throw E_JcefAppAlreadyInitialized()
			jcefStateObservers.add(observer)
		}
	}

	private val customSchemesRegistrants = ArrayList<JcefCustomSchemesRegistrant>()

	fun addCustomSchemes(registrant: JcefCustomSchemesRegistrant) {
		synchronized(Jcef) {
			if (jcefAppInitialized) throw E_JcefAppAlreadyInitialized()
			customSchemesRegistrants.add(registrant)
		}
	}

	inline val app: CefApp
		get() = @Suppress("DEPRECATION") CefAppSetup.app

	private class AppHandler(
		val jcefStateObservers: ArrayList<JcefStateObserver>,
		val customSchemesRegistrants: ArrayList<JcefCustomSchemesRegistrant>,
	) : CefAppHandlerAdapter(null) {

		override fun stateHasChanged(state: CefAppState) {
			if (state == CefAppState.TERMINATED) {
				val lock = CefAppTeardown.terminated_lock
				synchronized(lock) {
					CefAppTeardown.terminated = true
					lock.notifyAll()
				}
			}
			jcefStateObservers.fastForEach {
				it.onStateChanged(state)
			}
		}

		override fun onContextInitialized() {
			jcefStateObservers.fastForEach {
				it.onContextInitialized()
			}
		}

		override fun onRegisterCustomSchemes(registrar: CefSchemeRegistrar) {
			customSchemesRegistrants.fastForEach {
				it.onRegisterCustomSchemes(registrar)
			}
		}

		/**
		 * Necessary in order to avoid execution issues in macOS.
		 *
		 * @see me.friwi.jcefmaven.MavenCefAppHandlerAdapter
		 */
		override fun onBeforeCommandLineProcessing(process_type: String?, command_line: CefCommandLine?) {
			@Suppress("DEPRECATION") CefAppSetup.app.onBeforeCommandLineProcessing(process_type, command_line)
		}
	}

	@Deprecated(SPECIAL_USE_DEPRECATION)
	@PublishedApi
	internal object CefAppSetup {
		@JvmField val app: CefApp

		init {
			synchronized(Jcef) { jcefAppInitialized = true }

			if (CefApp.getState() != CefAppState.NONE) {
				// Must not let someone else initialize `CefApp`, since we would
				// like to exclusively customize `CefApp` only here.
				error("Someone else initialized `CefApp`")
			}

			CefApp.addAppHandler(AppHandler(
				jcefStateObservers.apply { trimToSize() },
				customSchemesRegistrants.apply { trimToSize() },
			))

			val cefSettings = CefSettings().apply {
				// Must be explicitly set to `false` or the entire UI (not just
				// the browser UI) will refuse input, as if frozen --
				// experienced on Windows 10; not sure on other OS though.
				windowless_rendering_enabled = false

				log_severity = CefSettings.LogSeverity.LOGSEVERITY_DISABLE
				// NOTE: Even if logging is disabled, provide a path still, in
				// case JCEF/CEF doesn't honor our request for disabled logging.
				log_file = AppData.mainLogsDir.toString() + File.separatorChar + "jcef.debug.log"

				// TODO How to handle locale changes while the app is already running?
				locale = currentLocale().toLanguageTag()

				// TODO
				//cache_path = ...
				//remote_debugging_port = ...
			}

			// Must use a mutable list here as `CefInitializer.initialize()` may
			// modify it to add its own entries.
			val cefArgs = ArrayList<String>(16)

			this.app = CefInitializer.initialize(bundleDir, cefArgs, cefSettings)

			CleanProcessExit.addHook(CPE_RANK, CefAppTeardown())
		}
	}

	const val CPE_RANK = 100

	private class CefAppTeardown : CleanProcessExit.Hook {
		companion object {
			@GuardedBy("terminated_lock")
			@JvmField var terminated = false
			@JvmField val terminated_lock = Object()
		}

		override fun onCleanup() {
			app.dispose() // Expected to kill all JCEF helpers

			val lock = terminated_lock
			synchronized(lock) {
				while (!terminated)
					lock.wait()
			}

			// Kills all JCEF helpers that should really not run anymore, to
			// prevent leaking them when our process isn't even running anymore.
			// - See also, https://bugs.openjdk.org/browse/JDK-4770092
			killDescendantJcefHelpers()
		}
	}

	fun killDescendantJcefHelpers() = killAllJcefHelpers(
		ProcessHandle.current().descendants(),
		detachedOnly = false, // From `descendants()` (and thus not yet detached)
	)

	fun killExtraneousJcefHelpers() = killAllJcefHelpers(
		ProcessHandle.allProcesses(),
		detachedOnly = true, // Avoid killing a `jcef_helper` process that is still in use
	)

	// CONTRACT: The value here must match that of `ProcessHandle.info().command()`
	private val jcefHelperPath: NioPath = File(bundleDir, when (EnumPlatform.getCurrentPlatform().os!!) {
		EnumOS.WINDOWS -> "jcef_helper.exe"
		EnumOS.MACOSX -> "jcef Helper.app/Contents/MacOS/jcef Helper" // TODO Verify if this is correct
		EnumOS.LINUX -> "jcef_helper" // TODO Verify if this is correct
	}).toNioPath().toAbsolutePath()

	private fun killAllJcefHelpers(processes: Stream<ProcessHandle>, detachedOnly: Boolean) {
		// TODO-FIXME Verify that our handling for non-Windows OS is correct
		processes.forEach { p ->
			p.info().command().getOrNull()?.let { command ->
				try {
					val commandPath = command.toNioPath().toAbsolutePath()
					if (commandPath.isSameFileAs(jcefHelperPath)) {
						if (!detachedOnly || p.parent().isEmpty) {
							p.descendants().forEach(ProcessHandle::destroy)
							p.destroy()
						}
					}
				} catch (ex: Throwable) {
					if (DEBUG) throw ex
				}
			}
		}
	}
}

private fun E_JcefBundleDirNotSet() = IllegalStateException(
	"""
	JCEF bundle location not set.
	Must either set system property "jcef.bundle" (pointing to the JCEF install
	directory or bundle), or set up environment variable "APP_HOME" (where
	"APP_HOME/jcef" is the JCEF install directory).
	""".trimIndent()
)

private fun E_JcefAppAlreadyInitialized() = IllegalStateException(
	"No longer possible. JCEF app already initialized."
)
