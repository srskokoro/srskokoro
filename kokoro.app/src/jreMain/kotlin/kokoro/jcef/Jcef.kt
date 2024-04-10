package kokoro.jcef

import androidx.annotation.GuardedBy
import kokoro.internal.CleanProcessExit
import kokoro.internal.DEBUG
import kokoro.internal.Os
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.check
import kokoro.internal.checkNotNull
import kokoro.internal.collections.fastForEachDeferringThrow
import kokoro.internal.collections.fastForEachIndexedDeferringThrow
import kokoro.internal.i18n.currentLocale
import kokoro.internal.io.NioPath
import kokoro.internal.io.toNioPath
import kotlinx.atomicfu.atomic
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.callback.CefCommandLine
import org.cef.callback.CefSchemeRegistrar
import org.cef.handler.CefAppHandlerAdapter
import java.io.File
import java.util.stream.Stream
import kotlin.io.path.isSameFileAs
import kotlin.jvm.optionals.getOrNull

object Jcef {

	inline val bundleDir: File get() = @Suppress("DEPRECATION_ERROR") Init.bundleDir

	inline val app: CefApp get() = @Suppress("DEPRECATION_ERROR") AppHolder.app

	fun init(config: JcefConfig) {
		@Suppress("DEPRECATION_ERROR")
		check(Init.bar.compareAndSet(false, true))

		if (CefApp.getState() != CefAppState.NONE) {
			// Must not let someone else initialize `CefApp`, since we would
			// like to exclusively customize `CefApp` only here.
			error("Someone else initialized `CefApp`")
		}

		CefApp.addAppHandler(AppHandler(
			ArrayList(config.customSchemes),
			ArrayList(config.stateObservers),
		))

		@Suppress("DEPRECATION_ERROR")
		val cefSettings = config.asCefSettings()

		val cefApp = JcefNatives.init(cefSettings.apply {
			// Must be explicitly set to `false` or the entire UI (not just
			// the browser UI) will refuse input, as if frozen --
			// experienced on Windows 10; not sure on other OS though.
			windowless_rendering_enabled = false

			// NOTE: At the moment, there's currently no known way to update the
			// locale once CEF has been initialized.
			// - See, https://www.magpcss.org/ceforum/viewtopic.php?f=6&t=12816
			locale = currentLocale().toLanguageTag()
		})

		@Suppress("DEPRECATION_ERROR")
		Init.app = cefApp

		CleanProcessExit.addHook(CPE_RANK, Teardown())
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi
	internal object Init {
		@JvmField internal val bar = atomic(false)
		@JvmField val bundleDir = JcefNatives.bundleDir // May throw
		@JvmField var app: CefApp? = null
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi
	internal object AppHolder {
		@Suppress("DEPRECATION_ERROR")
		val app = checkNotNull(Init.app, or = {
			"Must first call `${Jcef::class.simpleName}${::init.name}()`"
		})
	}

	private class AppHandler(
		val customSchemes: ArrayList<JcefCustomScheme>,
		val stateObservers: ArrayList<JcefStateObserver>,
	) : CefAppHandlerAdapter(null) {

		override fun stateHasChanged(state: CefAppState) {
			if (state == CefAppState.TERMINATED) {
				val lock = Teardown.terminated_lock
				synchronized(lock) {
					Teardown.terminated = true
					lock.notifyAll()
				}
			}
			stateObservers.fastForEachDeferringThrow {
				it.onStateChanged(state)
			}
		}

		override fun onContextInitialized() {
			stateObservers.fastForEachDeferringThrow {
				it.onContextInitialized()
			}
		}

		override fun onRegisterCustomSchemes(registrar: CefSchemeRegistrar) {
			customSchemes.fastForEachIndexedDeferringThrow(fun(i, entry) = entry.run {
				check(registrar.addCustomScheme(
					/*        schemeName = */ schemeName,
					/*        isStandard = */ isStandard,
					/*           isLocal = */ isLocal,
					/* isDisplayIsolated = */ isDisplayIsolated,
					/*          isSecure = */ isSecure,
					/*     isCorsEnabled = */ isCorsEnabled,
					/*    isCspBypassing = */ isCspBypassing,
					/*    isFetchEnabled = */ isFetchEnabled,
				), or = {
					"Registration failed for custom scheme: $schemeName\n" +
						"\n" +
						"- Index: $i\n" +
						"- Entry: $entry\n" +
						"\n" +
						"To resolve this issue, make sure that the custom scheme is registered only once\n" +
						"and that it isn't any of the built-in schemes (e.g., HTTP, HTTPS, FILE, FTP,\n" +
						"ABOUT, DATA, etc.)"
				})
			})
		}

		// Avoid execution issues on macOS. See, https://github.com/jcefmaven/jcefmaven/blob/122.1.10/jcefmaven/src/main/java/me/friwi/jcefmaven/MavenCefAppHandlerAdapter.java
		override fun onBeforeCommandLineProcessing(process_type: String?, command_line: CefCommandLine?) {
			app.onBeforeCommandLineProcessing(process_type, command_line)
		}
	}

	// --

	const val CPE_RANK = 100

	private class Teardown : CleanProcessExit.Hook {
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
	private val jcefHelperPath: NioPath = File(bundleDir, when (Os.current) {
		Os.WINDOWS -> "jcef_helper.exe"
		Os.MACOS -> "jcef Helper.app/Contents/MacOS/jcef Helper" // TODO Verify if this is correct
		Os.LINUX -> "jcef_helper" // TODO Verify if this is correct
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
