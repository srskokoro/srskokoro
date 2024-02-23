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
inline fun require(condition: Boolean, orFailWith: () -> Any) {
	contract {
		returns() implies condition
	}
	kotlin.require(condition, orFailWith)
}

/**
 * Alternative to [kotlin.require]`()` with better parameter names.
 */
inline fun <T : Any> requireNotNull(value: T?, orFailWith: () -> Any): T {
	contract {
		returns() implies (value != null)
	}
	return kotlin.requireNotNull(value, orFailWith)
}
