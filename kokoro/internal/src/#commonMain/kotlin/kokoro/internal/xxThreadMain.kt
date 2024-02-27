package kokoro.internal

expect fun isThreadMain(): Boolean

@Suppress("NOTHING_TO_INLINE")
inline fun assertThreadMain(
	or: () -> Any = { @Suppress("DEPRECATION_ERROR") checkThreadMain_lazyMessage() },
) = assert({ isThreadMain() }, or = or)

@Suppress("NOTHING_TO_INLINE")
inline fun checkThreadMain(
	or: () -> Any = { @Suppress("DEPRECATION_ERROR") checkThreadMain_lazyMessage() },
) = check(isThreadMain(), or = or)

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi internal expect fun checkThreadMain_lazyMessage(): String
