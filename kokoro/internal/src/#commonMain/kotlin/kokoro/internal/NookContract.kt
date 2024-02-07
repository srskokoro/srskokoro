package kokoro.internal

/**
 * Example usage:
 *
 * ```
 * @NookContract
 * @RequiresOptIn(NOOK)
 * annotation class nook
 * ```
 *
 * Meant to be opted in, module-wide, via compiler arguments.
 *
 * See, [Module-wide opt-in | Opt-in requirements | Kotlin Documentation](https://kotlinlang.org/docs/opt-in-requirements.html#module-wide-opt-in)
 *
 * @see NOOK
 */
@RequiresOptIn("Requires build plugin setup or manual module-wide opt-in via compiler arguments.")
annotation class NookContract
