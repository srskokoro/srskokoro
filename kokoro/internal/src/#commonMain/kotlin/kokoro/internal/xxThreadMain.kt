package kokoro.internal

expect fun isThreadMain(): Boolean

@Suppress(NOTHING_TO_INLINE)
inline fun assertThreadMain(
	orFailWith: () -> Any = { @Suppress(DEPRECATION_ERROR) checkThreadMain_lazyMessage() },
) = assert({ isThreadMain() }, orFailWith = orFailWith)

@Suppress(NOTHING_TO_INLINE)
inline fun checkThreadMain(
	orFailWith: () -> Any = { @Suppress(DEPRECATION_ERROR) checkThreadMain_lazyMessage() },
) = check(isThreadMain(), orFailWith = orFailWith)

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal expect fun checkThreadMain_lazyMessage(): String
