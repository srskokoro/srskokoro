package kokoro.internal.ui

import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.Icon
import javax.swing.JLabel

open class DummyComponent(
	text: String? = null, icon: Icon? = null,
	horizontalAlignment: Int = CENTER,
) : JLabel(text, icon, horizontalAlignment) {

	init {
		isFocusable = false
		isEnabled = false
	}

	// --

	override fun validate() {}
	override fun invalidate() {}
	override fun repaint() {}
	override fun revalidate() {}
	override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {}
	override fun repaint(r: Rectangle?) {}

	override fun paint(g: Graphics?) {}
	override fun getAccessibleContext() = null

	// --

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Any?, newValue: Any?) =
		super.firePropertyChange(propertyName, oldValue, newValue)

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Byte, newValue: Byte) {}

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Char, newValue: Char) {}

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Short, newValue: Short) {}

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Int, newValue: Int) {}

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Long, newValue: Long) {}

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Double, newValue: Double) {}

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Float, newValue: Float) {}

	/** Overridden for performance reasons. */
	final override fun firePropertyChange(propertyName: String?, oldValue: Boolean, newValue: Boolean) {}
}
