package kokoro.app.ui.swing

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.jthemedetecor.OsThemeDetector
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.assert
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.InvocationEvent
import java.lang.invoke.VarHandle
import java.util.function.Consumer
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

object AppLaf {

	@Suppress("NOTHING_TO_INLINE")
	inline fun ensure() = @Suppress("DEPRECATION_ERROR") AppLafSetup.maybeInit()

	inline val isDark get() = @Suppress("DEPRECATION_ERROR") _isDark

	/**
	 * Should be set to match the current LAF, which isn't necessarily when the
	 * system changes dark mode.
	 */
	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi @JvmField internal var _isDark = false
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi
internal object AppLafSetup :
	Throwable(null, null, false, false),
	Consumer<Boolean>, Runnable {

	// CONTRACT: MUST be regarded as immutable once set to any other value.
	@JvmField var nonInitStatus: Throwable? = this
	// ^ Not `private` to avoid the extra synthetic accessor.
	// ^ Deliberately not `@Volatile` -- it's OK for threads to not immediately see updates.

	private var isOsThemeDark: Boolean

	init {
		val detector = OsThemeDetector.getDetector()
		// NOTE: The listener registered below will only be called after an OS
		// theme change is detected, i.e., it won't be called until then.
		detector.registerListener(this)
		isOsThemeDark = detector.isDark
		// NOTE: `detector.isDark` must be queried 'after' registering as a
		// listener above. Otherwise, if it's queried 'before' the registration,
		// there's a possibility of a race with the OS changing the theme just
		// right after the query but before the registration; and yet, the
		// listener won't be called during registration, but only after another
		// OS theme change, thus causing us to miss the current theme change.
	}

	override fun accept(isOsThemeDark: Boolean) {
		// The following 'write' is guaranteed to *happen before* the
		// `invokeLater()`, even if the field wasn't marked volatile.
		this.isOsThemeDark = isOsThemeDark
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
			for (w in Window.getWindows()) {
				if (w is AppLafListener) w.onAppLafUpdated() // May throw; let it!
				SwingUtilities.updateComponentTreeUI(w) // May throw; let it!
			}

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
		val isDark = this.isOsThemeDark
		@Suppress("DEPRECATION_ERROR")
		AppLaf._isDark = isDark

		UIManager.setLookAndFeel(
			if (!isDark) {
				FlatLightLaf()
			} else {
				FlatDarkLaf()
			}
		)
	}

	// --

	private inline val hasInit: Boolean get() = nonInitStatus != this

	@Suppress("NOTHING_TO_INLINE")
	inline fun maybeInit() {
		if (nonInitStatus == null) return
		if (nonInitStatus == this) return initialize()
		throw wrapThrown()
	}

	fun initialize() {
		if (!EventQueue.isDispatchThread()) {
			awaitInitializeViaSwingEdt()
			return
		}

		// ---===--- ---===--- ---===---
		// In Swing EDT...

		try {
			// TODO More initialization logic goes here
			//  ...
			updateLaf()
		} catch (ex: Throwable) {
			VarHandle.releaseFence() // Prevent the update below from being seen too early
			nonInitStatus = ex // Prevent being called again by `maybeInit()`
			throw wrapThrown()
		}
		VarHandle.releaseFence() // Prevent the update below from being seen too early
		nonInitStatus = null // Prevent being called again by `maybeInit()`
	}

	private fun awaitInitializeViaSwingEdt() {
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
			assert(or = { thrown }, condition = {
				thrown is UnsupportedLookAndFeelException &&
					thrown.cause.let { it != null && it === nonInitStatus }
			})
			throw wrapThrown() // So that we get the correct stacktrace
		}
	}

	@PublishedApi
	internal fun wrapThrown(): UnsupportedLookAndFeelException {
		assert({ nonInitStatus != null }, or = { "Expected: thrown while initializing" })
		assert({ hasInit }, or = { "Should be considered initialized by now" })
		val wrapper = UnsupportedLookAndFeelException("Failed to initialize look and feel")
		wrapper.initCause(nonInitStatus)
		return wrapper
	}

	override fun fillInStackTrace(): Throwable =
		throw AssertionError("Should not be called")

	private fun readResolve(): Any = @Suppress("DEPRECATION_ERROR") AppLafSetup
}
