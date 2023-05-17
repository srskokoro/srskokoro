package kokoro.app.ui

import kokoro.app.i18n.Locale
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.UIDefaults
import javax.swing.UIManager
import kotlin.math.max

actual suspend fun Alerts.await(handler: AlertHandler, spec: AlertSpec): AlertButton? = TODO("Not yet implemented")

actual enum class AlertStyle(internal val value: Int) {
	PLAIN(JOptionPane.PLAIN_MESSAGE),
	ERROR(JOptionPane.ERROR_MESSAGE),
	WARN(JOptionPane.WARNING_MESSAGE),
	QUESTION(JOptionPane.QUESTION_MESSAGE),
	INFO(JOptionPane.INFORMATION_MESSAGE),
}

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
	}
}
