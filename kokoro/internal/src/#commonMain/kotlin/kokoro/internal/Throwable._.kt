package kokoro.internal

/**
 * @see Throwable.addSuppressed
 */
expect inline fun Throwable.addSuppressed_(exception: Throwable)

expect fun Throwable.throwAnySuppressed()
