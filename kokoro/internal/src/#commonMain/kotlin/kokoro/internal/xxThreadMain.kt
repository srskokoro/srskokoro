package kokoro.internal

expect fun isThreadMain(): Boolean

@Suppress(NOTHING_TO_INLINE)
inline fun assertThreadMain(
	or_fail_with: () -> Any = { @Suppress(DEPRECATION_ERROR) checkThreadMain_lazyMessage() },
) = assert({ isThreadMain() }, or_fail_with = or_fail_with)

@Suppress(NOTHING_TO_INLINE)
inline fun checkThreadMain(
	or_fail_with: () -> Any = { @Suppress(DEPRECATION_ERROR) checkThreadMain_lazyMessage() },
) = check(isThreadMain(), or_fail_with = or_fail_with)

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal expect fun checkThreadMain_lazyMessage(): String
