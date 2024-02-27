@file:Suppress("NOTHING_TO_INLINE")

package kokoro.internal

const val ASSERTIONS_ENABLED = !IS_RELEASING

/** See, [KT-22292](https://youtrack.jetbrains.com/issue/KT-22292) */
inline fun assert(condition: () -> Boolean, or: () -> Any = { "Assertion failed!" }) {
	if (ASSERTIONS_ENABLED && !condition()) {
		throw AssertionError(or())
	}
}

inline fun assertUnreachable(or: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = or()
		throw AssertionError(message)
	}
}

inline fun assertUnreachable(cause: Throwable?, or: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = or().toString()
		throw AssertionError(message, cause)
	}
}

inline fun assertSucceeds(statement: () -> Unit, or: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) try {
		statement()
	} catch (ex: Throwable) {
		val message = or().toString()
		throw AssertionError(message, ex)
	}
}

/**
 * Similar to [kotlin.error]`()` but throws [AssertionError] instead.
 *
 * It's a less verbose way of writing,
 * ```
 * throw AssertionError(message)
 * ```
 */
inline fun errorAssertion(message: Any): Nothing = throw AssertionError(message)
