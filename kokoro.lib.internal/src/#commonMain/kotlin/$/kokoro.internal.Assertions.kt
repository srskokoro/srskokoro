@file:Suppress("PackageDirectoryMismatch")

import kokoro.internal.IS_RELEASING
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@PublishedApi
internal const val ASSERTIONS_ENABLED = !IS_RELEASING

/** See, [KT-22292](https://youtrack.jetbrains.com/issue/KT-22292) */
inline fun assert(lazyMessage: () -> Any = { "Assertion failed!" }, lazyCheck: () -> Boolean) {
	if (ASSERTIONS_ENABLED && !lazyCheck()) {
		throw AssertionError(lazyMessage())
	}
}

inline fun assertUnreachable(lazyMessage: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = lazyMessage()
		throw AssertionError(message)
	}
}

/**
 * Throws an [AssertionError] if the [value] is null.
 */
@Suppress("NOTHING_TO_INLINE")
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

/**
 * Throws an [AssertionError] if the value produced by [lazyValue] is null.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> assertNotNull(lazyValue: () -> T?) =
	assertNotNull({ "Required value was null." }, lazyValue)

/**
 * Throws an [AssertionError] with the result of calling [lazyMessage] if the
 * value produced by [lazyValue] is null.
 */
inline fun <T : Any> assertNotNull(lazyMessage: () -> Any, lazyValue: () -> T?) {
	if (ASSERTIONS_ENABLED && lazyValue() == null) {
		val message = lazyMessage()
		throw AssertionError(message)
	}
}
