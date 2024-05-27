package kokoro.app.ui.swing

import java.awt.Component
import java.awt.Window
import kotlin.contracts.contract

/**
 * Returns the [Window] ancestor of this component, or this component itself if
 * it's [Window]; `null` if this component is not a part of a window hierarchy.
 *
 * (Similar to [sun.awt.SunToolkit.getContainingWindow]`()` utility method.)
 */
fun Component?.getContainingWindow(): Window? {
	contract {
		returnsNotNull() implies (this@getContainingWindow != null)
	}
	var comp = this
	while (comp != null && comp !is Window) {
		comp = comp.parent
	}
	return comp as Window?
}
