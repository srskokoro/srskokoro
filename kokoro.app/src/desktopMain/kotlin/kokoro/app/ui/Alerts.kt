package kokoro.app.ui

import com.formdev.flatlaf.FlatLaf
import kokoro.app.i18n.Locale
import kokoro.internal.ui.DummyComponent
import kokoro.internal.ui.NopCloseUiAction
import kokoro.internal.ui.assertThreadSwing
import kokoro.internal.ui.checkThreadSwing
import kokoro.internal.ui.ensureBounded
import kokoro.internal.ui.repack
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Font
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.util.LinkedList
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JWindow
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

	val relative = parent ?: BaseAppWindow.lastActive
	pane.componentOrientation = (relative ?: JOptionPane.getRootFrame()).componentOrientation

	val title = kotlin.run {
		val base = spec.titleBase
		val main = spec.title

		if (base == null) main
		else if (main == null) base
		else "$base \u2013 $main"
	}

	val dialog: JDialog = if (parent != null) {
		pane.createDialog(parent, title)
	} else {
		pane.createDialog(title) // Will have its own system taskbar entry
	}
	dialog.modalityType = Dialog.ModalityType.DOCUMENT_MODAL // Won't be set automatically for us :P

	inflater.setPaneRef(pane, dialog)

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

	dialog.ensureBounded(if (spec.isResizable) {
		// Set this first, since on some platforms, changing the resizable state
		// affects the insets of the dialog.
		dialog.isResizable = true
		dialog.repack() // Necessary in case the insets changed
		spec.ensureBoundedByMaxDiv
	} else 1)

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
	CustomAction {
		override fun AlertButtonInflater.getText(): String = @Suppress("DEPRECATION") AlertButtonImplCommon.TEXT_DEFAULT_CustomAction
		override fun AlertButtonInflater.getIcon(): Icon? = null
		override fun AlertButtonInflater.getMnemonic(): Int = 0
	},
	;

	actual override val choice: AlertChoice get() = this
	actual override val textOverride: Nothing? get() = null

	internal abstract fun AlertButtonInflater.getText(): String?
	internal abstract fun AlertButtonInflater.getIcon(): Icon?
	internal abstract fun AlertButtonInflater.getMnemonic(): Int
}

internal class AlertButtonInflater {
	private class OptionPaneRef {
		@JvmField var pane: JOptionPane? = null
		@JvmField var dialog: JDialog? = null
	}

	private val paneRef = OptionPaneRef()
	private val componentsWithMnemonicTips = LinkedList<OptionPaneButton>()

	fun setPaneRef(pane: JOptionPane, dialog: JDialog) {
		paneRef.pane = pane
		paneRef.dialog = dialog

		componentsWithMnemonicTips.let {
			if (it.isEmpty()) return // Done. There's nothing to do.

			MnemonicTipsDispatcher(it).addTo(dialog.rootPane)
		}
	}

	// --

	@JvmField val locale: Locale = JComponent.getDefaultLocale()
	@JvmField val uiManager: UIDefaults = UIManager.getDefaults()

	private val buttonMinimumWidth = uiManager.getNumberToInt("OptionPane.buttonMinimumWidth", locale)
	private val buttonClickThreshhold = uiManager.getNumberToInt("OptionPane.buttonClickThreshhold", locale)
	private val buttonFont: Font? = uiManager.getFont("OptionPane.buttonFont", locale)

	@Suppress("NOTHING_TO_INLINE")
	private inline fun UIDefaults.getNumberToInt(key: String, locale: Locale): Int =
		get(key, locale).let { if (it is Number) it.toInt() else 0 }

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun getString(key: String): String? = uiManager.getString(key, locale)

	@Suppress("NOTHING_TO_INLINE")
	inline fun getIcon(key: String): Icon? = uiManager.getIcon(key, locale)

	@Suppress("NOTHING_TO_INLINE")
	inline fun getMnemonic(key: String): Int {
		val s = uiManager.get(key, locale)
		if (s is String) try {
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
					componentsWithMnemonicTips.addLast(component)
				}
			}
		}

		component.name = "OptionPane.button"
		component.multiClickThreshhold = buttonClickThreshhold.toLong()
		buttonFont?.let { component.font = it }

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
			paneRef.pane?.value = template
		}

		// --

		private var mnemonicTip: JWindow? = null
		private var mnemonicTipGap = 0

		fun onShowMnemonicTip(show: Boolean) {
			var tip = mnemonicTip
			if (!show) {
				tip?.isVisible = false
				return // Skip everything below
			} else if (tip == null) {
				// Inspired by, https://stackoverflow.com/a/65744432
				// --

				val tipLabel = JLabel(KeyEvent.getKeyText(mnemonic))
				tipLabel.isOpaque = true

				val uiManager = UIManager.getDefaults()
				tipLabel.foreground = uiManager.getColor("ToolTip.foreground")
				tipLabel.background = uiManager.getColor("ToolTip.background")
				tipLabel.font = uiManager.getFont("ToolTip.font")
				tipLabel.border = uiManager.getBorder("ToolTip.border")

				// `parent` must not be null, since we'll rely on it to dispose
				// the popup window for us.
				val parent = paneRef.dialog!!

				// Get the gap size used by `GroupLayout` -- see, https://stackoverflow.com/a/29167736
				mnemonicTipGap = LayoutStyle.getInstance().getPreferredGap(this, tipLabel,
					LayoutStyle.ComponentPlacement.RELATED, SOUTH, parent)

				tip = JWindow(parent)
				mnemonicTip = tip

				tip.type = Window.Type.POPUP
				tip.focusableWindowState = false
				tip.contentPane.add(tipLabel)
				tip.pack()
			}

			// From `(n + 1) >> 1`; from `floor((n + 1) / 2.0)`; from `floor(n / 2.0 + 0.5)`; from `round(n / 2.0)`
			var x = ((width - tip.width) + 1) shr 1
			// ^ WARNING: An arithmetic shift performs a 'floor', while an
			// integer division performs a truncation. Thus, DO NOT change the
			// above to its "integer division" counterpart.

			val tipGap = mnemonicTipGap
			var y = height + tipGap

			if (isShowing) {
				val loc = locationOnScreen
				x += loc.x
				y += loc.y

				graphicsConfiguration?.let { gc ->
					val sb = gc.bounds // Screen bounds
					val si = toolkit.getScreenInsets(gc)

					if ((y + tip.height) > (sb.y + sb.height - si.bottom)) {
						y = loc.y - tip.height - tipGap
					}
				}
			}

			tip.setLocation(x, y)
			tip.isVisible = true
		}
	}

	private class MnemonicTipsDispatcher(
		private val registered: LinkedList<OptionPaneButton>
	) : DummyComponent(), Runnable {
		private var prevIsShowMnemonics = false

		override fun run() {
			val oldIsShowMnemonics = prevIsShowMnemonics
			val newIsShowMnemonics = FlatLaf.isShowMnemonics()
			if (oldIsShowMnemonics != newIsShowMnemonics) {
				this.prevIsShowMnemonics = newIsShowMnemonics
				for (it in registered) {
					it.onShowMnemonicTip(newIsShowMnemonics)
				}
			}
		}

		override fun repaint() {
			if (EventQueue.isDispatchThread()) run()
			else EventQueue.invokeLater(this)
		}

		// Must be non-negative to force FlatLaf to call `repaint()` on mnemonic display trigger
		override fun getDisplayedMnemonicIndex() = 0

		override fun setDisplayedMnemonicIndex(index: Int) {}
	}
}

//endregion
