package kokoro.internal.ui

import java.awt.Component
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
