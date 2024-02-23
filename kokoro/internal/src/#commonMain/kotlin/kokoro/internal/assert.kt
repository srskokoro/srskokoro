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
