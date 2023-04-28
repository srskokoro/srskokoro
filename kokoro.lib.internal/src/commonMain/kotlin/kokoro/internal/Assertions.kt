package kokoro.internal

@PublishedApi
internal const val ASSERTIONS_ENABLED = !IS_RELEASING

// See, https://youtrack.jetbrains.com/issue/KT-22292
inline fun assert(lazyMessage: () -> Any = { "Assertion failed!" }, lazyCheck: () -> Boolean) {
	if (ASSERTIONS_ENABLED && !lazyCheck()) {
		throw AssertionError(lazyMessage())
	}
}
