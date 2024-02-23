package kokoro.internal

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.use as kotlin_use

/**
 * @see kotlin.use
 * @see useIn
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	return kotlin_use(block)
}

/**
 * @see kotlin.use
 * @see use
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <T : AutoCloseable?, R> T.useIn(block: T.() -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	return kotlin_use(block)
}
