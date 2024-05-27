package kokoro.app.ui.swing

import java.awt.Component
import java.awt.Window

fun Window.setLocationBesides(c: Component?) {
	run op@{
		if (c == null) return@op
		if (!c.isShowing) return@op
		val ccw = c.getContainingWindow() ?: return@op

		val gc = ccw.graphicsConfiguration
		val gb = gc.usableBounds

		val gx = gb.x
		val gx_end = gx + gb.width

		val gy = gb.y
		val gy_end = gy + gb.height

		val sw = width
		val sh = height

		var dx: Int
		var dy: Int

		run<Unit> {
			val cl = c.locationOnScreen
			val cy = cl.y
			dy = cy

			val cx = cl.x
			val csw = c.width
			dx = cx + csw // Try placing at the right side of the component
			if (dx + sw <= gx_end) return@run

			dx = cx - sw // Try placing at the left side of the component
			if (dx >= gx) return@run

			// Center horizontally relative to the component
			dx = cx + (csw - sw) / 2

			val csh = c.height
			dy = cy + csh // Try placing below the component
			if (dy + sh <= gy_end) return@run

			dy = cy - sh // Try placing above the component
			if (dy >= gy) return@run

			// Center vertically relative to the component
			dy = cy + (csh - sh) / 2
		}

		// Avoid being placed off the edge of the screen
		if (dx + sw > gx_end) dx = gx_end - sw
		if (dx < gx) dx = gx
		if (dy + sh > gy_end) dy = gy_end - sh
		if (dy < gy) dy = gy

		setLocation(dx, dy)
		return // Done. Skip code below.
	}

	setLocationRelativeTo(c)
}
