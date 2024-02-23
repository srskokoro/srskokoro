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
 * NOTE: Unlike with `@`[RequiresOptIn]`()`, marking symbols as `@`[Deprecated]`()`
 * appear specially in IDE autocompletion popups (e.g., the symbols appear in
 * strike-through style), and thus, their special nature is conveyed even before
 * being entered into code.
 *
 * @see Deprecated
 * @see SPECIAL_USE_DEPRECATION_FOR_TESTS
 * @see NOOK
 * @see DEPRECATION_ERROR
 */
const val SPECIAL_USE_DEPRECATION = "Should not be used directly"

/**
 * Similar to [SPECIAL_USE_DEPRECATION] but with an extra clause explaining that
 * the deprecated symbol is exposed only for access in tests.
 *
 * @see SPECIAL_USE_DEPRECATION
 * @see DEPRECATION_ERROR
 */
const val SPECIAL_USE_DEPRECATION_FOR_TESTS = "$SPECIAL_USE_DEPRECATION: exposed only for access in tests"
