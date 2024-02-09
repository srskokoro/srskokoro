package kokoro.internal

const val ASSERTIONS_ENABLED = !IS_RELEASING

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
