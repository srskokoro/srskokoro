package kokoro.internal.ui

import java.awt.Dimension
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
	val screenSize = graphicsConfiguration.bounds.size

	val maxWidth = screenSize.width / maxDiv
	val windowWidth = width

	val newWidth = if (maxWidth < windowWidth) {
		shouldResize = true
		maxWidth
	} else {
		windowWidth
	}

	val maxHeight = screenSize.height / maxDiv
	val windowHeight = height

	val newHeight = if (maxHeight < windowHeight) {
		shouldResize = true
		maxHeight
	} else {
		windowHeight
	}

	if (shouldResize) setSize(newWidth, newHeight)

	// ---===--- ---===--- ---===---

	var minWidth = screenSize.width / 4
	if (minWidth < 100) minWidth *= 2

	val curWidth = width
	if (curWidth < minWidth) minWidth = curWidth

	var minHeight = screenSize.height / 4
	if (minHeight < 100) minHeight *= 2

	val curHeight = height
	if (curHeight < minHeight) minHeight = curHeight

	this.minimumSize = Dimension(minWidth, minHeight)
}
