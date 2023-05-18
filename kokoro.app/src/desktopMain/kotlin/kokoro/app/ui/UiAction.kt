package kokoro.app.ui

import java.beans.PropertyChangeListener
import javax.swing.Action

/**
 * @see sun.swing.UIAction
 */
abstract class UiAction(val name: String?) : Action {
	companion object {
		const val CLOSE = "close"
	}

	override fun getValue(key: String?): Any? =
		@Suppress("ReplaceCallWithBinaryOperator")
		if (Action.NAME.equals(key)) name else null

	override fun putValue(key: String?, value: Any?) {}

	override fun setEnabled(newValue: Boolean) {}

	override fun addPropertyChangeListener(listener: PropertyChangeListener?) {}

	override fun removePropertyChangeListener(listener: PropertyChangeListener?) {}
}