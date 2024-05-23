package kokoro.app.ui.swing

import java.awt.event.ActionEvent
import java.beans.PropertyChangeListener
import javax.swing.Action

/**
 * An [Action] implementation that is always disabled.
 *
 * @see UiAction
 */
object NopAction : Action {

	override fun getValue(key: String?): Any? = null

	override fun putValue(key: String?, value: Any?) {}

	override fun setEnabled(newValue: Boolean) {}

	override fun isEnabled(): Boolean = false

	override fun addPropertyChangeListener(listener: PropertyChangeListener?) {}

	override fun removePropertyChangeListener(listener: PropertyChangeListener?) {}

	override fun actionPerformed(e: ActionEvent?) {}
}
