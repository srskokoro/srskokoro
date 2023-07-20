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
 * @see SPECIAL_USE_IN_TESTS_DEPRECATION
 */
const val SPECIAL_USE_DEPRECATION = "Should not be used directly"

/**
 * Similar to [SPECIAL_USE_DEPRECATION] but with an extra clause explaining that
 * the deprecated symbol is exposed only for access in tests.
 *
 * @see SPECIAL_USE_DEPRECATION
 */
const val SPECIAL_USE_IN_TESTS_DEPRECATION = "$SPECIAL_USE_DEPRECATION: exposed only for access in tests"
