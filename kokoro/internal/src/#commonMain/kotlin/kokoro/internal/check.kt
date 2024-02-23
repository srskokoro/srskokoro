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
inline fun check(condition: Boolean, orFailWith: () -> Any) {
	contract {
		returns() implies condition
	}
	kotlin.check(condition, orFailWith)
}

/**
 * Alternative to [kotlin.check]`()` with better parameter names.
 */
inline fun <T : Any> checkNotNull(value: T?, orFailWith: () -> Any): T {
	contract {
		returns() implies (value != null)
	}
	return kotlin.checkNotNull(value, orFailWith)
}
