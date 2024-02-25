package kokoro.internal

const val ASSERTIONS_ENABLED = !IS_RELEASING

/** See, [KT-22292](https://youtrack.jetbrains.com/issue/KT-22292) */
inline fun assert(condition: () -> Boolean, or_fail_with: () -> Any = { "Assertion failed!" }) {
	if (ASSERTIONS_ENABLED && !condition()) {
		throw AssertionError(or_fail_with())
	}
}

inline fun assertUnreachable(or_fail_with: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = or_fail_with()
		throw AssertionError(message)
	}
}

inline fun assertUnreachable(cause: Throwable?, or_fail_with: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = or_fail_with().toString()
		throw AssertionError(message, cause)
	}
}

inline fun assertSucceeds(statement: () -> Unit, or_fail_with: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) try {
		statement()
	} catch (ex: Throwable) {
		val message = or_fail_with().toString()
		throw AssertionError(message, ex)
	}
}
