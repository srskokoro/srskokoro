package build.test

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf

/**
 * @see assertWith
 */
inline fun <R> assertResult(block: () -> R) = assertThat(runCatching(block))

inline fun <reified T : Throwable> Assert<Result<*>>.isFailure() = isFailure().isInstanceOf<T>()
