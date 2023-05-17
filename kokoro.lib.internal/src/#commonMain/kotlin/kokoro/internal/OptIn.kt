package kokoro.internal

/**
 * Used as a message for `@`[Deprecated]`()`, for declarations that are
 * technically not obsolete, but rather, should simply be not used directly, as
 * they are intended for special, internal use.
 *
 * This can be used as an alternative to the missing "package-private"
 * visibility in Kotlin &ndash; see, [KT-29227](https://youtrack.jetbrains.com/issue/KT-29227).
 *
 * Example usage:
 * ```
 * @Deprecated(SPECIAL_USE_DEPRECATION)
 * ```
 *
 * @see Deprecated
 */
const val SPECIAL_USE_DEPRECATION = "Should not be used"
