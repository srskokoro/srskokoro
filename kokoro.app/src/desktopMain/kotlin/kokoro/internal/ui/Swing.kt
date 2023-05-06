package kokoro.internal.ui

import java.awt.Component
import java.awt.Dimension
import java.awt.Window
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Suppress("NOTHING_TO_INLINE")
inline fun <C : Component> C.ifVisible(): C? = when {
	isVisible -> this
	else -> null
}

@OptIn(ExperimentalContracts::class)
inline fun <C : Component, R> C.ifVisible(block: (component: C) -> R): R? {
	contract {
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
	return when {
		isVisible -> block(this)
		else -> null
	}
}

/**
 * Calls [pack][Window.pack]`()`, followed by [ensureBounded]`()`
 *
 * @see Window.ensureBounded
 */
fun Window.boundedPack(maxDiv: Int = 1) {
	pack()
	ensureBounded(maxDiv)
}

/** @see Window.boundedPack */
fun Window.ensureBounded(maxDiv: Int = 1) {
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
