package kokoro.app.ui.swing

import java.awt.GraphicsConfiguration
import java.awt.Insets
import java.awt.Rectangle
import java.awt.Toolkit

val GraphicsConfiguration.insets: Insets
	inline get() = Toolkit.getDefaultToolkit().getScreenInsets(this)

/**
 * @see sun.java2d.SunGraphicsEnvironment.getUsableBounds
 */
val GraphicsConfiguration.usableBounds: Rectangle
	get() {
		val insets = insets
		val usableBounds = bounds
		usableBounds.x += insets.left
		usableBounds.y += insets.top
		usableBounds.width -= insets.left + insets.right
		usableBounds.height -= insets.top + insets.bottom
		return usableBounds
	}
