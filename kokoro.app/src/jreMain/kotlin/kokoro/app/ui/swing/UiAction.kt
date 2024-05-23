package kokoro.app.ui.swing

import java.beans.PropertyChangeListener
import javax.swing.Action

/**
 * Base class for UI actions used in [ActionMap][javax.swing.ActionMap] –
 * similar to [sun.swing.UIAction] class.
 *
 * @see NopAction
 * @see javax.swing.AbstractAction
 * @see com.formdev.flatlaf.ui.FlatUIAction
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
