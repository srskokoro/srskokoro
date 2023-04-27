package kokoro.app.ui

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.jthemedetecor.OsThemeDetector
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.InvocationEvent
import java.util.function.Consumer
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

@Suppress("NOTHING_TO_INLINE")
internal inline fun ensureAppLaf() {
	val ex = AppLafSetup.thrown
	if (ex != null) throw wrap(ex)
}

// --

private fun wrap(ex: Throwable): UnsupportedLookAndFeelException {
	val wrapper = UnsupportedLookAndFeelException("Failed to initialize look and feel")
	wrapper.initCause(ex)
	return wrapper
}

private object AutoDarkAppLaf : Consumer<Boolean>, Runnable {
	@JvmField var isDark: Boolean

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

			// NOTE: Access to `AppLafSetup` blocks until it's fully initialized
			try {
				AppLafSetup.run()
			} catch (ex: Throwable) {
				AppLafSetup.thrown = ex
				return // Skip everything below
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
}

internal object AppLafSetup : Runnable {
	@JvmField var thrown: Throwable?

	init {
		if (EventQueue.isDispatchThread()) {
			try {
				run()
				thrown = null
			} catch (ex: Throwable) {
				thrown = ex
			}
		} else {
			// The following mimics `EventQueue.invokeAndWait()` but without
			// throwing on thread interrupts.

			@Suppress("RemoveRedundantQualifierName", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
			class AWTInvocationLock : java.lang.Object()

			val lock = AWTInvocationLock()

			val toolkit = Toolkit.getDefaultToolkit()
			val event = InvocationEvent(toolkit, this, lock, true)

			var interrupted = false

			val queue = toolkit.systemEventQueue
			synchronized(lock) {
				queue.postEvent(event)
				while (!event.isDispatched) {
					try {
						@Suppress("BlockingMethodInNonBlockingContext")
						lock.wait()
					} catch (ex: InterruptedException) {
						interrupted = true
						// Proceed as if we weren't interrupted
					}
				}
			}

			thrown = event.throwable

			if (interrupted) // Restore "interrupted" status
				Thread.currentThread().interrupt()
		}
	}

	override fun run() {
		UIManager.setLookAndFeel(
			if (!AutoDarkAppLaf.isDark) {
				FlatLightLaf()
			} else {
				FlatDarkLaf()
			}
		)
	}
}
