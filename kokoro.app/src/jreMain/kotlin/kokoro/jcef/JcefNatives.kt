package kokoro.jcef

import kokoro.internal.DEBUG
import kokoro.internal.check
import kokoro.internal.checkNotNull
import kotlinx.atomicfu.atomic
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.OS
import org.cef.SystemBootstrap
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

// NOTE: The following is a heavily modified version of `CefInitializer.java`
// from the "JCEF Maven" codebase -- https://github.com/jcefmaven/jcefmaven/blob/122.1.10/jcefmaven/src/main/java/me/friwi/jcefmaven/impl/step/init/CefInitializer.java
//
// Necessary since we don't want to depend on the "JCEF Maven" library, as it
// comes with its own logic for downloading and setting up the natives (which is
// something we don't use or need).
//
object JcefNatives {

	private val LOG = Logger.getLogger(JcefNatives::class.java.getName())

	// The "Java library path" system property used for loading native
	// libraries.
	//
	// - On Windows, it defaults to the `PATH` environment variable.
	// - On macOS, is defaults to the `DYLD_LIBRARY_PATH` environment variable.
	// - On Linux, it defaults to the `LD_LIBRARY_PATH` environment variable.
	//
	// See also,
	// - https://stackoverflow.com/a/20038808
	// - https://stackoverflow.com/q/29968292
	private const val JAVA_LIBRARY_PATH = "java.library.path"

	// --

	private val bundleDir_ = atomic<File?>(null)

	private const val bundleDir__name = "bundleDir"

	val bundleDir: File
		get() = checkNotNull(bundleDir_.value, or = {
			"Must first call `$init__name($bundleDir__name)`"
		})

	/**
	 * NOTE: Can't use `::`[init]`.name` due to overload ambiguity; so we
	 * created this constant.
	 */
	private const val init__name = "init"

	fun init(bundleDir: File) {
		require(bundleDir.isAbsolute) { "`$bundleDir__name` needs to be absolute." }
		require(bundleDir.isDirectory) { "`$bundleDir__name` must be an existing directory." }
		check(bundleDir_.compareAndSet(null, bundleDir)) { E_CALL_ONCE }
		// Patch the Java library path to include the `bundleDir`.
		// - This is required for JCEF to find all resources.
		//
		// NOTE: At this point, it may be too late to patch "java.library.path"
		// for loading native libraries.
		// - See, https://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html
		// - However, JCEF does parse this to scan for other resources. That's
		// why we're patching it.
		System.setProperty(JAVA_LIBRARY_PATH, buildString {
			val prop: String? = System.getProperty(JAVA_LIBRARY_PATH)
			if (!prop.isNullOrEmpty()) {
				append(prop)
				if (!prop.endsWith(File.pathSeparatorChar))
					append(File.pathSeparatorChar)
			}
			append(bundleDir)
		})
	}

	// --

	private val init = atomic(false)

	fun init(cefSettings: CefSettings): CefApp {
		val bundleDir = bundleDir // May throw
		check(init.compareAndSet(false, true)) { E_CALL_ONCE }

		// Prevent JCEF from loading any native libraries, as it causes
		// unnecessary errors due to incorrect library names in JCEF.
		SystemBootstrap.setLoader { /* NOP */ }

		loadJAWT() // Must manually load this (if not already loaded)

		var cefArgs = emptyArray<String>()
		@Suppress("UnsafeDynamicallyLoadedCode")
		if (OS.isWindows()) {
			System.load(File(bundleDir, "chrome_elf.dll").path)
			System.load(File(bundleDir, "libcef.dll").path) // CEF natives
			System.load(File(bundleDir, "jcef.dll").path) // JCEF natives
			cefSettings.browser_subprocess_path = File(bundleDir, "jcef_helper.exe").path
		} else if (OS.isLinux()) {
			System.load(File(bundleDir, "libcef.so").path) // CEF natives
			System.load(File(bundleDir, "libjcef.so").path) // JCEF natives

			cefSettings.browser_subprocess_path = File(bundleDir, "jcef_helper").path
			cefSettings.resources_dir_path = bundleDir.path
			cefSettings.locales_dir_path = File(bundleDir, "locales").path

			if (!CefApp.startup(cefArgs)) throw E_CefStartupFailed()
		} else if (OS.isMacintosh()) {
			System.load(File(bundleDir, "libjcef.dylib").path) // JCEF natives

			val bundleDirPath = bundleDir.path
			val browser_subprocess_path = "$bundleDirPath/jcef Helper.app/Contents/MacOS/jcef Helper"
			cefSettings.browser_subprocess_path = browser_subprocess_path

			cefArgs = arrayOf(
				"--browser-subprocess-path=$browser_subprocess_path",
				"--main-bundle-path=$bundleDirPath/jcef Helper.app",
				"--framework-dir-path=$bundleDirPath/Chromium Embedded Framework.framework",
			)

			if (!CefApp.startup(cefArgs)) throw E_CefStartupFailed()
		}

		val cefApp = CefApp.getInstance(cefArgs, cefSettings)

		// ASSUMPTION: At this point, all necessary native libraries have
		// already been loaded.
		SystemBootstrap.setLoader {
			if (DEBUG) throw AssertionError("" +
				"All necessary native libraries should've been loaded already, " +
				"and yet, an attempt was made to load another native library: " + it
			).also { ex ->
				// Log in case the error above won't be shown due to JVM crash.
				LOG.log(Level.SEVERE, ex.message, ex)
				LOG.log(Level.SEVERE, "--")
			}
			System.loadLibrary(it)
		}

		return cefApp
	}

	private fun loadJAWT() {
		// From, https://github.com/JFormDesigner/FlatLaf/blob/3.4.1/flatlaf-core/src/main/java/com/formdev/flatlaf/ui/FlatNativeLibrary.java#L99
		//
		// - Load "jawt" (part of JRE) explicitly because it is not found in all
		// Java versions/distributions, e.g., not found in Java 13 and later
		// from `openjdk.java.net`; there seems to be also differences between
		// distributions, e.g., Adoptium Java 17 does not need this, but Java 17
		// from `openjdk.java.net` does.
		try {
			System.loadLibrary("jawt")
		} catch (ex: UnsatisfiedLinkError) {
			// Log error only if native library was not already loaded
			val message = ex.message
			if (message == null || !message.contains("already loaded in another classloader"))
				LOG.log(Level.SEVERE, message, ex)
		} catch (ex: Exception) {
			LOG.log(Level.SEVERE, ex.message, ex)
		}
	}

	private const val E_CALL_ONCE = "Can only be called once."

	private fun E_CefStartupFailed() = IllegalStateException("CEF startup failed.")
}
