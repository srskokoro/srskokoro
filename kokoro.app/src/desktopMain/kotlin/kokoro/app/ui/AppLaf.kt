package kokoro.app.ui

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import com.jthemedetecor.OsThemeDetector
import java.awt.Component
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.event.InvocationEvent
import java.lang.reflect.InvocationTargetException
import java.util.function.Consumer
import javax.swing.SwingUtilities
import javax.swing.UIManager

@Suppress("NOTHING_TO_INLINE")
internal inline fun ensureAppLaf() {
	val ex = AppLafSetup.thrown
	if (ex != null) throw wrap(ex)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun ensureAppLaf(component: Component) {
	ensureAppLaf()
	SwingUtilities.updateComponentTreeUI(component)
}

// --

private fun wrap(ex: Throwable) = InvocationTargetException(ex)

internal object AppLafSetup : Runnable {
	@JvmField var thrown: Throwable?
	@JvmField var isDark: Boolean

	init {
		val detector = OsThemeDetector.getDetector()
		// NOTE: The listener registered below will only be called after an OS
		// theme change is detected, i.e., it won't be called until then.
		detector.registerListener(AutoDarkAppLaf())
		val isDark = detector.isDark
		// NOTE: `detector.isDark` must be queried 'after' registering the
		// listener above. Otherwise, if it's queried before the registration,
		// there's a possibility of a race with the OS changing the theme just
		// right after the query but before the registration; and yet, the
		// listener won't be called during registration, but only after another
		// OS theme change, thus causing us to miss the current theme change.
		this.isDark = isDark

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

			if (interrupted)
				Thread.currentThread().interrupt()
		}
	}

	override fun run() {
		UIManager.setLookAndFeel(if (!isDark) FlatLightLaf() else FlatDarkLaf())
	}
}

private class AutoDarkAppLaf : Consumer<Boolean>, Runnable {

	override fun accept(isDark: Boolean) {
		// The following 'write' is guaranteed to *happen before* the
		// `invokeLater()`, even if the field wasn't marked volatile.
		AppLafSetup.isDark = isDark // May block until `LafSetup` is fully initialized
		EventQueue.invokeLater(this)
	}

	override fun run() {
		// Unless `LafSetup` has fully initialized, any access to it will block.
		try {
			AppLafSetup.run()
		} catch (ex: Throwable) {
			AppLafSetup.thrown = ex
		}
	}
}
