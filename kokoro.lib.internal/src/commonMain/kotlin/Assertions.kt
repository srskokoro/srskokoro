package kokoro.internal

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PublishedApi
internal const val ASSERTIONS_ENABLED = !IS_RELEASING

@OptIn(ExperimentalContracts::class)
// See, https://youtrack.jetbrains.com/issue/KT-22292
inline fun assert(lazyMessage: () -> Any = { "Assertion failed!" }, lazyCheck: () -> Boolean) {
	contract {
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
		callsInPlace(lazyCheck, InvocationKind.AT_MOST_ONCE)
	}
	if (ASSERTIONS_ENABLED && lazyCheck()) {
		throw AssertionError(lazyMessage())
	}
}
