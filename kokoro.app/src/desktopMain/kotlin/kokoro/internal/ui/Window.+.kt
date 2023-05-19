package kokoro.internal.ui

import java.awt.Dimension
import java.awt.Frame
import java.awt.Window

/**
 * Similar to [Window.pack]`()` but without causing the `Window` to be validated
 * again. It also doesn't cause it to be displayable (if not displayable yet).
 *
 * This method is intended to be called after [Window.pack]`()` has already been
 * called.
 */
fun Window.repack() {
	val d = preferredSize
	setSize(d.width, d.height)
}

/**
 * Calls [pack][Window.pack]`()`, followed by [ensureBounded]`()`
 *
 * @see Window.boundedRepack
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Window.boundedPack(maxDiv: Int = 1) {
	pack()
	ensureBounded(maxDiv)
}

/**
 * Similar to [Window.boundedPack]`()` but calls [repack]`()` instead.
 *
 * This method is intended to be called after [Window.pack]`()` has already been
 * called.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Window.boundedRepack(maxDiv: Int = 1) {
	repack()
	ensureBounded(maxDiv)
}

/** @see Window.boundedPack */
@Suppress("NOTHING_TO_INLINE")
inline fun Window.ensureBounded() = ensureBounded(1)

/** @see Window.boundedPack */
fun Window.ensureBounded(maxDiv: Int) {
	require(maxDiv > 0) { "Positive integer required" }

	var shouldResize = false
	val screenBounds = graphicsConfiguration.bounds

	val maxWidth = screenBounds.width / maxDiv
	val windowWidth = width

	val newWidth = if (maxWidth < windowWidth) {
		shouldResize = true
		maxWidth
	} else {
		windowWidth
	}

	val maxHeight = screenBounds.height / maxDiv
	val windowHeight = height

	val newHeight = if (maxHeight < windowHeight) {
		shouldResize = true
		maxHeight
	} else {
		windowHeight
	}

	if (shouldResize) setSize(newWidth, newHeight)

	// ---===--- ---===--- ---===---

	var minWidth = screenBounds.width / 4
	if (minWidth < 100) minWidth *= 2

	val curWidth = width
	if (curWidth < minWidth) minWidth = curWidth

	var minHeight = screenBounds.height / 4
	if (minHeight < 100) minHeight *= 2

	val curHeight = height
	if (curHeight < minHeight) minHeight = curHeight

	this.minimumSize = Dimension(minWidth, minHeight)
}

@Suppress("NOTHING_TO_INLINE")
@JvmName("setLocationCascade_nullable")
inline fun Window.setLocationCascade(lastFrame: Frame?) {
	if (lastFrame == null || !lastFrame.isDisplayable) {
		isLocationByPlatform = true
	} else setLocationCascade(lastFrame)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Window.setLocationCascadeOrCenter(lastFrame: Frame?) {
	if (lastFrame == null || !lastFrame.isDisplayable) {
		setLocationRelativeTo(null)
	} else setLocationCascade(lastFrame)
}

fun Window.setLocationCascade(lastFrame: Frame) {
	kotlin.run {
		val lastFrameState = lastFrame.extendedState
		if (!lastFrame.isShowing || (lastFrameState and Frame.MAXIMIZED_BOTH) != 0) return@run

		val lastLoc = if ((lastFrameState and Frame.ICONIFIED) == 0) {
			// Case: It's currently NOT minimized
			lastFrame.locationOnScreen // ASSUMPTION: More reliable than `getLocation()`
		} else {
			// Case: It's currently minimized
			lastFrame.location
		}

		val gc = graphicsConfiguration
		val sb = gc.bounds
		val si = toolkit.getScreenInsets(gc)

		val insets = insets

		// Use the title bar height as offset
		val cascadeOffset = lastFrame.insets.top

		val y = lastLoc.y + cascadeOffset
		if ((y + (insets.top + insets.bottom) * 2) > (sb.y + sb.height - si.bottom)) return@run

		var x = lastLoc.x + cascadeOffset - insets.left
		val excessX = (x + width) - (sb.x + sb.width - si.right)
		if (excessX > 0) x -= excessX

		val sx = sb.x + si.left
		if (sx > x) x = sx

		setLocation(x, y)
		return // Skip code below
	}
	isLocationByPlatform = true
}
