package build.api.provider

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ProviderFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Counterpart of [ProviderFactory.runUntracked] that may return a value.
 *
 * @see Project.callUntracked
 * @see Settings.callUntracked
 */
@OptIn(ExperimentalContracts::class)
inline fun <R> ProviderFactory.callUntracked(crossinline block: () -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	var result: R
	runUntracked {
		result = block()
	}
	return result
}

/**
 * Counterpart of [Project.runUntracked] that may return a value.
 *
 * @see ProviderFactory.callUntracked
 */
@OptIn(ExperimentalContracts::class)
inline fun <R> Project.callUntracked(crossinline block: () -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	return providers.callUntracked(block)
}

/**
 * Counterpart of [Settings.runUntracked] that may return a value.
 *
 * @see ProviderFactory.callUntracked
 */
@OptIn(ExperimentalContracts::class)
inline fun <R> Settings.callUntracked(crossinline block: () -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	return providers.callUntracked(block)
}
