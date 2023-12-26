package build.api.provider

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A scope for running code with configuration input tracking disabled.
 *
 * This type is primarily useful for creating extension functions that should
 * only run under the said conditions of this scope.
 *
 * See also,
 * - [Improvements in the Build Configuration Input Tracking | Gradle Blog](https://blog.gradle.org/improvements-in-the-build-configuration-input-tracking)
 * - [Add more APIs to `File`-related configuration input tracking · Issue #23638 · gradle/gradle](https://github.com/gradle/gradle/issues/23638)
 *
 * @see ProviderFactory.runUntracked
 */
sealed interface UntrackedScope

/**
 * Runs the specified [block] with configuration input tracking disabled.
 *
 * See [UntrackedScope] for more info.
 *
 * @see ProviderFactory.callUntracked
 * @see Project.runUntracked
 * @see Settings.runUntracked
 */
@OptIn(ExperimentalContracts::class)
fun ProviderFactory.runUntracked(block: UntrackedScope.() -> Unit) {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	RunUntrackedHelper.runAction(block, this)
}

/**
 * @see Project.callUntracked
 * @see ProviderFactory.runUntracked
 */
@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
inline fun Project.runUntracked(noinline block: UntrackedScope.() -> Unit) {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	return providers.runUntracked(block)
}

/**
 * @see Settings.callUntracked
 * @see ProviderFactory.runUntracked
 */
@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
inline fun Settings.runUntracked(noinline block: UntrackedScope.() -> Unit) {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	return providers.runUntracked(block)
}

// --

internal abstract class RunUntrackedHelper : ValueSource<Boolean, RunUntrackedHelper.Parameters> {

	interface Parameters : ValueSourceParameters {
		val ticket: Property<Long>
	}

	private data object DefaultUntrackedScope : UntrackedScope

	companion object {
		private val nextTicket = AtomicLong()
		private val actions = ConcurrentHashMap<Long, UntrackedScope.() -> Unit>()

		fun runAction(action: UntrackedScope.() -> Unit, providers: ProviderFactory) {
			val ticket = nextTicket.getAndIncrement()
			if (actions.putIfAbsent(ticket, action) != null) {
				throw Error("Maximum number of tickets reached!")
			}
			providers.of(RunUntrackedHelper::class.java) {
				parameters.ticket.set(ticket)
			}.get()
		}
	}

	override fun obtain(): Boolean {
		val ticket = parameters.ticket.get()
		actions.remove(ticket)?.invoke(DefaultUntrackedScope)
		return false
	}
}
