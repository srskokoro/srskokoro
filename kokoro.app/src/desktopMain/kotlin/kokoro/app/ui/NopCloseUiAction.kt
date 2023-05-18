package kokoro.app.ui

import java.awt.event.ActionEvent
import javax.swing.ActionMap

object NopCloseUiAction : UiAction(CLOSE) {

	@Suppress("NOTHING_TO_INLINE")
	inline fun addTo(actionMap: ActionMap) = actionMap.put(CLOSE, this)

	// --

	override fun isEnabled() = false

	override fun actionPerformed(e: ActionEvent) = Unit
}
