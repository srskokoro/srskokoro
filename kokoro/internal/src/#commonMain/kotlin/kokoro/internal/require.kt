@file:Suppress("NOTHING_TO_INLINE")

package kokoro.internal

import kotlin.contracts.contract

/**
 * Alternative to [kotlin.require]`()` with better parameter names.
 */
inline fun require(condition: Boolean) {
	contract {
		returns() implies condition
	}
	kotlin.require(condition)
}

/**
 * Alternative to [kotlin.require]`()` with better parameter names.
 */
inline fun require(condition: Boolean, or: () -> Any) {
	contract {
		returns() implies condition
	}
	kotlin.require(condition, or)
}

/**
 * Alternative to [kotlin.requireNotNull]`()` with better parameter names.
 */
inline fun <T : Any> requireNotNull(value: T?, or: () -> Any): T {
	contract {
		returns() implies (value != null)
	}
	return kotlin.requireNotNull(value, or)
}
