package kokoro.app.ui

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import com.jthemedetecor.OsThemeDetector
import java.awt.Component
import java.awt.EventQueue
import java.lang.reflect.InvocationTargetException
import java.util.function.Consumer
import javax.swing.SwingUtilities
import javax.swing.UIManager

@Suppress("NOTHING_TO_INLINE")
internal inline fun ensureAppLaf() {
	val ex = `-AppLaf-thrown`
	if (ex != null) throw wrap(ex)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun ensureAppLaf(component: Component) {
	ensureAppLaf()
	SwingUtilities.updateComponentTreeUI(component)
}

private fun wrap(ex: Throwable) = InvocationTargetException(ex)

@Suppress("ObjectPropertyName")
// WARNING: Needs to be set before `init`
@JvmField internal var `-AppLaf-thrown`: Throwable? = null
// ^ `internal` (and not `private`) to avoid synthetic accessor.

@Suppress("unused") val init: Unit = run {
	val listener = LafFixer
	val detector = OsThemeDetector.getDetector()
	detector.registerListener(listener)
	// Must manually invoke the listener 'right after' registering it (and not
	// before it), as the listener will only be called after an OS theme change
	// is detected, i.e., it won't be called until then.
	listener.accept(detector.isDark) // NOTE: May set `-AppLaf-thrown`
	// We shouldn't invoke the listener 'before registration' as there's a
	// possibility of a race with the OS changing the theme right after the
	// invocation but before the registration -- and remember, the listener
	// won't be called during registration, but rather, only after another OS
	// theme change.
}

private object LafFixer : Consumer<Boolean>, Runnable {
	// Volatile isn't necessary here. See comment in `accept()`.
	private var isDark: Boolean = false

	override fun accept(isDark: Boolean) {
		// The following operation is guaranteed to *happen before* the
		// `invokeAndWait()`, even if the variable isn't volatile.
		this.isDark = isDark

		if (EventQueue.isDispatchThread()) {
			run()
		} else {
			EventQueue.invokeAndWait(this)
		}
	}

	override fun run() {
		try {
			UIManager.setLookAndFeel(if (!isDark) FlatLightLaf() else FlatDarkLaf())
		} catch (ex: Throwable) {
			OsThemeDetector.getDetector().removeListener(this)
			`-AppLaf-thrown` = ex
		}
	}
}
