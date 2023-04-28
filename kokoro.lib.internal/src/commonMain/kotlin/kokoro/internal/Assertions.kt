package kokoro.internal

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@PublishedApi
internal const val ASSERTIONS_ENABLED = !IS_RELEASING

// See, https://youtrack.jetbrains.com/issue/KT-22292
inline fun assert(lazyMessage: () -> Any = { "Assertion failed!" }, lazyCheck: () -> Boolean) {
	if (ASSERTIONS_ENABLED && !lazyCheck()) {
		throw AssertionError(lazyMessage())
	}
}

/**
 * Throws an [AssertionError] if the [value] is null.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T : Any> assertNotNull(value: T?) {
	contract {
		returns() implies (value != null)
	}
	assertNotNull(value) { "Required value was null." }
}

/**
 * Throws an [AssertionError] with the result of calling [lazyMessage] if the
 * [value] is null.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T : Any> assertNotNull(value: T?, lazyMessage: () -> Any) {
	contract {
		returns() implies (value != null)
	}
	if (ASSERTIONS_ENABLED && value == null) {
		val message = lazyMessage()
		throw AssertionError(message)
	}
}
