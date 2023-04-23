package kokoro.internal

// See, https://youtrack.jetbrains.com/issue/KT-22292
inline fun assert(lazyMessage: () -> Any = { "Assertion failed!" }, lazyCheck: () -> Boolean) {
	if (!IS_RELEASING && lazyCheck()) {
		throw AssertionError(lazyMessage())
	}
}
