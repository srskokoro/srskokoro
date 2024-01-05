package build.test

import assertk.Assert
import assertk.assertThat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see assertResult
 */
@OptIn(ExperimentalContracts::class)
inline fun <R> assertWith(block: () -> R): Assert<R> {
	contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
	return assertThat(block())
}
