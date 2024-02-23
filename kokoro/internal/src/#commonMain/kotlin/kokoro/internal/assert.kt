package kokoro.internal

const val ASSERTIONS_ENABLED = !IS_RELEASING

/** See, [KT-22292](https://youtrack.jetbrains.com/issue/KT-22292) */
inline fun assert(condition: () -> Boolean, orFailWith: () -> Any = { "Assertion failed!" }) {
	if (ASSERTIONS_ENABLED && !condition()) {
		throw AssertionError(orFailWith())
	}
}

inline fun assertUnreachable(orFailWith: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = orFailWith()
		throw AssertionError(message)
	}
}

inline fun assertUnreachable(cause: Throwable?, orFailWith: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = orFailWith().toString()
		throw AssertionError(message, cause)
	}
}

inline fun assertSucceeds(statement: () -> Unit, orFailWith: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) try {
		statement()
	} catch (ex: Throwable) {
		val message = orFailWith().toString()
		throw AssertionError(message, ex)
	}
}
