package kokoro.app.ui.swing

import kokoro.internal.DEBUG
import java.awt.event.ActionEvent
import javax.swing.ActionMap

fun ActionMap.put(action: UiAction) {
	val name = action.name
	if (DEBUG) requireNotNull(name) { "`UiAction` should have a nonnull `name`" }
	// NOTE: If `name` is null, `action` is ignored (nothing is mapped).
	return put(name, action)
}

inline fun ActionMap.put(
	name: String,
	crossinline action: (ActionEvent) -> Unit,
) = put(name, UiAction(name, action))
