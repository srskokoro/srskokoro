@file:Suppress("NOTHING_TO_INLINE")

package kokoro.internal

import kotlin.contracts.contract

/**
 * Alternative to [kotlin.check]`()` with better parameter names.
 */
inline fun check(condition: Boolean) {
	contract {
		returns() implies condition
	}
	kotlin.check(condition)
}

/**
 * Alternative to [kotlin.check]`()` with better parameter names.
 */
inline fun check(condition: Boolean, or_fail_with: () -> Any) {
	contract {
		returns() implies condition
	}
	kotlin.check(condition, or_fail_with)
}

/**
 * Alternative to [kotlin.check]`()` with better parameter names.
 */
inline fun <T : Any> checkNotNull(value: T?, or_fail_with: () -> Any): T {
	contract {
		returns() implies (value != null)
	}
	return kotlin.checkNotNull(value, or_fail_with)
}
