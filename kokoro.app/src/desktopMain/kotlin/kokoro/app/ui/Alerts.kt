package kokoro.app.ui

import kokoro.app.i18n.Locale
import kokoro.internal.ui.assertThreadSwing
import kokoro.internal.ui.checkThreadSwing
import kokoro.internal.ui.ensureBounded
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JOptionPane
import javax.swing.JToolTip
import javax.swing.LayoutStyle
import javax.swing.UIDefaults
import javax.swing.UIManager
import javax.swing.WindowConstants
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

//region

actual suspend fun Alerts.await(handler: AlertHandler, spec: AlertSpec): AlertButton? {
	return suspendCancellableCoroutine { continuation ->
		val impl = AlertAwaitImpl(handler, spec, continuation)
		if (EventQueue.isDispatchThread()) impl.run()
		else EventQueue.invokeLater(impl)
	}
}

private class AlertAwaitImpl(
	private val client: AlertHandler, private val spec: AlertSpec,
	private val continuation: CancellableContinuation<AlertButton?>,
) : AlertHandler, Runnable {

	override fun run() {
		val continuation = continuation
		continuation.resume(try {
			val parent = null // TODO Infer via `continuation.context`
			Alerts.swing(this, parent, spec)
		} catch (ex: Throwable) {
			continuation.resumeWithException(ex)
			return
		})
	}

	override fun onShow(token: AlertToken) {
		continuation.invokeOnCancellation(token as AlertTokenImpl)
		client.onShow(token)
	}
}

//endregion

//region Counterpart in "Swing"

inline fun Alerts.swing(parent: Component?, spec: AlertSpec.() -> Unit) = swing(AlertHandler.DEFAULT, parent, spec)

@Suppress("NOTHING_TO_INLINE")
inline fun Alerts.swing(parent: Component?, spec: AlertSpec) = swing(AlertHandler.DEFAULT, parent, spec)

inline fun Alerts.swing(handler: AlertHandler, parent: Component?, spec: AlertSpec.() -> Unit) = swing(handler, parent, AlertSpec().apply(spec))

@Suppress("UnusedReceiverParameter")
fun Alerts.swing(handler: AlertHandler, parent: Component?, spec: AlertSpec): AlertButton? {
	checkThreadSwing()
	ensureAppLaf()

	val inflater = AlertButtonInflater()
	val buttons = spec.buttons
	val components: Array<JButton> = buttons.mapToTypedArray(inflater::inflate)
	val defaultComponent = spec.defaultButton.let {
		if (it >= 0 && it < components.size)
			components[it] else null
	}
	val isNonCancellable = buttons.isNonCancellable

	val messageType = spec.style.value
	val pane = JOptionPane(
		spec.message,
		messageType,
		JOptionPane.DEFAULT_OPTION,
		null,
		components,
		defaultComponent,
	)
	inflater.paneRef.value = pane

	val relative = parent ?: BaseAppWindow.lastActive
	pane.componentOrientation = (relative ?: JOptionPane.getRootFrame()).componentOrientation

	val dialog =
		if (parent != null) pane.createDialog(parent, spec.title)
		else pane.createDialog(spec.title) // Will have its own system taskbar entry

	if (!isNonCancellable) {
		// We're cancellable/closeable
		WindowConstants.HIDE_ON_CLOSE
	} else {
		// Necessary to prevent `Esc` key "close" action (which is otherwise
		// still enabled even if `defaultCloseOperation` is configured).
		NopCloseUiAction.addTo(pane.actionMap)
		// We're NOT cancellable/closeable
		WindowConstants.DO_NOTHING_ON_CLOSE
	}.let {
		dialog.defaultCloseOperation = it
	}

	dialog.ensureBounded(spec.ensureBoundedByMaxDiv)
	dialog.isResizable = spec.isResizable
	dialog.setLocationRelativeTo(relative)

	// NOTE: The following not only gives the client code an opportunity to
	// dismiss the dialog, but also, to dismiss it before it could be shown.
	handler.onShow(AlertTokenImpl(dialog, pane))

	// Before we proceed, check that we're not disposed yet (since the last
	// operation above may have triggered an early disposal).
	if (dialog.isDisplayable) {
		playSystemSound(messageType)
		dialog.isVisible = true // Will block and spawn a secondary event loop
		dialog.dispose() // Done!
	}

	// NOTE: The `value` below can be an `Int`, a `String`, or null, as the
	// dialog can be dismissed by means other than our custom button components,
	// and there may be internal code beyond our grasp that could set it to
	// something else.
	return pane.value as? AlertButton
}

private fun playSystemSound(messageType: Int) {
	val soundProp = when (messageType) {
		JOptionPane.PLAIN_MESSAGE -> return // Skip. No sound requested.

		// Only Windows is supported (for now). See, https://bugs.openjdk.org/browse/JDK-8149630
		// - See also, https://www.autohotkey.com/docs/v2/lib/MsgBox.htm#Group_2_Icon
		JOptionPane.ERROR_MESSAGE -> "win.sound.hand"
		JOptionPane.WARNING_MESSAGE -> "win.sound.exclamation"
		JOptionPane.QUESTION_MESSAGE -> "win.sound.question"
		JOptionPane.INFORMATION_MESSAGE -> "win.sound.asterisk"

		else -> throw AssertionError(messageType)
	}

	val toolkit = Toolkit.getDefaultToolkit()

	toolkit.getDesktopProperty(soundProp).let {
		if (it is Runnable) {
			it.run() // Will play the system sound for us
			return // Done!
		}
	}

	// Fallback (and for other desktop platforms)
	toolkit.beep()
}

private class AlertTokenImpl(
	private val dialog: JDialog,
	private val pane: JOptionPane,
) : AlertToken, Runnable, CompletionHandler {
	private var deferredValue: AlertButton? = null

	override fun invoke(cause: Throwable?) = dismiss()

	override fun dismiss(choice: AlertButton?) {
		// The following 'write' is guaranteed to *happen before* the
		// `invokeLater()`, even if the field isn't marked volatile.
		deferredValue = choice
		if (EventQueue.isDispatchThread()) run()
		else EventQueue.invokeLater(this)
	}

	override fun run() {
		assertThreadSwing()
		// Now before we proceed, check that we're not disposed yet, since
		// someone else might have already disposed us. Also, the following
		// should be done only once; thus, we must check.
		if (dialog.isDisplayable) {
			pane.value = deferredValue
			dialog.dispose()
		}
	}
}

//endregion

//region Style

actual enum class AlertStyle(internal val value: Int) {
	PLAIN(JOptionPane.PLAIN_MESSAGE),
	ERROR(JOptionPane.ERROR_MESSAGE),
	WARN(JOptionPane.WARNING_MESSAGE),
	QUESTION(JOptionPane.QUESTION_MESSAGE),
	INFO(JOptionPane.INFORMATION_MESSAGE),
}

//endregion

//region Buttons

actual sealed interface AlertButton {
	actual val choice: AlertChoice
	actual val textOverride: Any?
}

actual enum class AlertChoice : AlertButton {
	OK {
		override fun AlertButtonInflater.getText(): String? = getString("OptionPane.okButtonText")
		override fun AlertButtonInflater.getIcon(): Icon? = getIcon("OptionPane.okIcon")
		override fun AlertButtonInflater.getMnemonic(): Int = getMnemonic("OptionPane.okButtonMnemonic")
	},
	Cancel {
		override fun AlertButtonInflater.getText(): String? = getString("OptionPane.cancelButtonText")
		override fun AlertButtonInflater.getIcon(): Icon? = getIcon("OptionPane.cancelIcon")
		override fun AlertButtonInflater.getMnemonic(): Int = getMnemonic("OptionPane.cancelButtonMnemonic")
	},
	Yes {
		override fun AlertButtonInflater.getText(): String? = getString("OptionPane.yesButtonText")
		override fun AlertButtonInflater.getIcon(): Icon? = getIcon("OptionPane.yesIcon")
		override fun AlertButtonInflater.getMnemonic(): Int = getMnemonic("OptionPane.yesButtonMnemonic")
	},
	No {
		override fun AlertButtonInflater.getText(): String? = getString("OptionPane.noButtonText")
		override fun AlertButtonInflater.getIcon(): Icon? = getIcon("OptionPane.noIcon")
		override fun AlertButtonInflater.getMnemonic(): Int = getMnemonic("OptionPane.noButtonMnemonic")
	},
	;

	actual override val choice: AlertChoice get() = this
	actual override val textOverride: Nothing? get() = null

	internal abstract fun AlertButtonInflater.getText(): String?
	internal abstract fun AlertButtonInflater.getIcon(): Icon?
	internal abstract fun AlertButtonInflater.getMnemonic(): Int
}

internal class AlertButtonInflater {
	class OptionPaneRef {
		@JvmField var value: JOptionPane? = null
	}

	@JvmField val paneRef = OptionPaneRef()

	@JvmField val locale: Locale = JComponent.getDefaultLocale()
	@JvmField val uiManager: UIDefaults = UIManager.getDefaults()

	private val buttonMinimumWidth = uiManager.getInt("OptionPane.buttonMinimumWidth", locale)

	@Suppress("NOTHING_TO_INLINE")
	inline fun getString(key: String): String? = uiManager.getString(key, locale)

	@Suppress("NOTHING_TO_INLINE")
	inline fun getIcon(key: String): Icon? = uiManager.getIcon(key, locale)

	@Suppress("NOTHING_TO_INLINE")
	inline fun getMnemonic(key: String): Int {
		val s = uiManager.getString(key, locale)
		if (s != null) try {
			return s.toInt()
		} catch (_: NumberFormatException) {
			// Ignore.
		}
		return 0
	}

	fun inflate(template: AlertButton): JButton {
		val component: OptionPaneButton
		template.choice.run {
			val textOverride = template.textOverride
			component = OptionPaneButton(
				text =
				if (textOverride == null) getText()
				else textOverride.toString(),
				icon = getIcon(),
				template, paneRef, buttonMinimumWidth,
			)

			val mnemonic = getMnemonic()
			if (mnemonic != 0) {
				component.mnemonic = mnemonic
				if (textOverride != null) {
					component.displayedMnemonicIndex = -1
					component.toolTipText = KeyEvent.getKeyText(mnemonic)
				}
			}
		}

		component.name = "OptionPane.button"
		component.multiClickThreshhold = uiManager.getInt("OptionPane.buttonClickThreshhold", locale).toLong()

		uiManager.getFont("OptionPane.buttonFont", locale)
			?.let { component.font = it }

		return component
	}

	private class OptionPaneButton(
		text: String?, icon: Icon?,
		private val template: AlertButton,
		private val paneRef: OptionPaneRef,
		private val minimumWidth: Int,
	) : JButton(text, icon) {

		override fun getMinimumSize(): Dimension {
			val min = super.getMinimumSize()
			val constraint = minimumWidth
			if (constraint > 0) min.width = max(min.width, constraint)
			return min
		}

		override fun getPreferredSize(): Dimension {
			val pref = super.getPreferredSize()
			val constraint = minimumWidth
			if (constraint > 0) pref.width = max(pref.width, constraint)
			return pref
		}

		override fun fireActionPerformed(event: ActionEvent) {
			paneRef.value?.value = template
		}

		// --

		private var tip: JToolTip? = null
		private var tipGap = 0

		override fun createToolTip(): JToolTip {
			var tip = tip
			if (tip == null) {
				tip = JToolTip()
				tip.component = this

				// Necessary in order to get the tooltip's preferred size early
				tip.tipText = toolTipText

				// Get the gap size used by `GroupLayout` -- see, https://stackoverflow.com/a/29167736
				tipGap = LayoutStyle.getInstance().getPreferredGap(this, tip,
					LayoutStyle.ComponentPlacement.RELATED, SOUTH, null)
			}
			return tip
		}

		override fun getToolTipLocation(event: MouseEvent?): Point {
			val tip = createToolTip()
			val tipPrefSize = tip.preferredSize

			val tipGap = tipGap
			var y = height + tipGap

			if (isShowing) graphicsConfiguration?.let { gc ->
				val edgeY = gc.bounds.let {
					// Take into account screen insets; decrease viewport.
					it.y + it.height - toolkit.getScreenInsets(gc).bottom
				}
				val tipHeight = tipPrefSize.height
				if (locationOnScreen.y + y + tipHeight > edgeY) {
					y = -(tipHeight + tipGap)
				}
			}

			// From `(n + 1) >> 1`; from `floor((n + 1) / 2.0)`; from `floor(n / 2.0 + 0.5)`; from `round(n / 2.0)`
			val x = ((width - tipPrefSize.width) + 1) shr 1
			// ^ WARNING: An arithmetic shift performs a 'floor', while an
			// integer division performs a truncation. Thus, DO NOT change the
			// above to its "integer division" counterpart.

			return Point(x, y)
		}
	}
}

//endregion
