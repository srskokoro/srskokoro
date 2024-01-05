package build.test

import assertk.assertThat

/**
 * @see assertWith
 */
inline fun <R> assertResult(block: () -> R) = assertThat(runCatching(block))
