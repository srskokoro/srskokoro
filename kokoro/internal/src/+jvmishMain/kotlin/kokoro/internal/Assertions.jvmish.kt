package kokoro.internal

inline fun assertSucceeds(lazyMessage: () -> Any = { "Assertion failed!" }, lazyCheck: () -> Unit) {
	if (ASSERTIONS_ENABLED) try {
		lazyCheck()
	} catch (ex: Throwable) {
		val message = lazyMessage().toString()
		throw AssertionError(message, ex)
	}
}

inline fun assertUnreachable(lazyMessage: () -> Any, cause: Throwable?) = assertUnreachable(cause, lazyMessage)

inline fun assertUnreachable(cause: Throwable?, lazyMessage: () -> Any = { "Should be unreachable" }) {
	if (ASSERTIONS_ENABLED) {
		val message = lazyMessage().toString()
		throw AssertionError(message, cause)
	}
}
