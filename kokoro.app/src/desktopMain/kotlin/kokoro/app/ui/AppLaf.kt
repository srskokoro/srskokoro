package kokoro.app.ui

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.jthemedetecor.OsThemeDetector
import kokoro.internal.assert
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.InvocationEvent
import java.util.function.Consumer
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

@Suppress("NOTHING_TO_INLINE")
internal inline fun ensureAppLaf() = AppLafSetup.maybeInit()

// --

internal object AppLafSetup :
	Throwable(null, null, false, false),
	Consumer<Boolean>, Runnable {

	@JvmField var noninit: Throwable? = this
	// ^ Not `private` to avoid the extra synthetic accessor

	private var isDark: Boolean

	init {
		val detector = OsThemeDetector.getDetector()
		// NOTE: The listener registered below will only be called after an OS
		// theme change is detected, i.e., it won't be called until then.
		detector.registerListener(this)
		isDark = detector.isDark
		// NOTE: `detector.isDark` must be queried 'after' registering as a
		// listener above. Otherwise, if it's queried 'before' the registration,
		// there's a possibility of a race with the OS changing the theme just
		// right after the query but before the registration; and yet, the
		// listener won't be called during registration, but only after another
		// OS theme change, thus causing us to miss the current theme change.
	}

	override fun accept(isDark: Boolean) {
		// The following 'write' is guaranteed to *happen before* the
		// `invokeLater()`, even if the field wasn't marked volatile.
		this.isDark = isDark
		EventQueue.invokeLater(this)
	}

	override fun run() {
		try {
			FlatAnimatedLafChange.showSnapshot()
			// ---===--- ---===--- ---===---

			if (hasInit) {
				updateLaf()
			} else {
				// Got called early (due to other means of class initialization)
				initialize()
			}

			// Also update existing windows
			for (w in Window.getWindows())
				SwingUtilities.updateComponentTreeUI(w) // May throw; let it!

			// ---===--- ---===--- ---===---
			FlatAnimatedLafChange.hideSnapshotWithAnimation()
		} catch (ex: Throwable) {
			try {
				FlatAnimatedLafChange.stop()
			} catch (exx: Throwable) {
				ex.addSuppressed(exx)
			}
			throw ex
		}
	}

	private fun updateLaf() {
		UIManager.setLookAndFeel(
			if (!isDark) {
				FlatLightLaf()
			} else {
				FlatDarkLaf()
			}
		)
	}

	// --

	private inline val hasInit: Boolean get() = noninit != this

	@Suppress("NOTHING_TO_INLINE")
	inline fun maybeInit() {
		if (noninit == null) return
		if (noninit == this) return initialize()
		throw wrapThrown()
	}

	fun initialize() {
		if (!EventQueue.isDispatchThread()) {
			initializeViaEdt()
			return
		}
		try {
			noninit = null // Prevent being called again by `maybeInit()`
			// TODO More initialization logic goes here
			//  ...
			updateLaf()
		} catch (ex: Throwable) {
			noninit = ex
			throw wrapThrown()
		}
	}

	private fun initializeViaEdt() {
		// The following mimics `EventQueue.invokeAndWait()` but without
		// throwing on thread interrupts.

		@Suppress("RemoveRedundantQualifierName", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
		class AWTInvocationLock : java.lang.Object()

		val lock = AWTInvocationLock()

		val toolkit = Toolkit.getDefaultToolkit()
		val event = InvocationEvent(toolkit, { maybeInit() }, lock, true)

		var interrupted = false

		val queue = toolkit.systemEventQueue
		synchronized(lock) {
			queue.postEvent(event)
			while (!event.isDispatched) {
				try {
					lock.wait()
				} catch (ex: InterruptedException) {
					interrupted = true
					// Proceed as if we weren't interrupted
				}
			}
		}

		val thrown = event.throwable

		if (interrupted) // Restore "interrupted" status
			Thread.currentThread().interrupt()

		if (thrown != null) {
			assert({ thrown }) {
				thrown is UnsupportedLookAndFeelException &&
				thrown.cause.let { it != null && it === noninit }
			}
			throw wrapThrown() // So that we get the correct stacktrace
		}
	}

	private fun wrapThrown(): UnsupportedLookAndFeelException {
		assert({ "Should be considered initialized by now" }) { hasInit }
		val wrapper = UnsupportedLookAndFeelException("Failed to initialize look and feel")
		wrapper.initCause(noninit)
		return wrapper
	}

	override fun fillInStackTrace(): Throwable =
		throw AssertionError("Should not be called")
}
