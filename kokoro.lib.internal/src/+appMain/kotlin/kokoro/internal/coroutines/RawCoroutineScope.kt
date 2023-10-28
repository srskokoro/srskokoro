package kokoro.internal.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Creates a [CoroutineScope] that wraps the given coroutine [context] and
 * overrides the context's [Job].
 *
 * Using this is slightly more efficient than using the `CoroutineScope()`
 * factory function.
 */
fun RawCoroutineScope(context: CoroutineContext, job: Job): CoroutineScope = ContextScope(context + job)

/**
 * Creates a [CoroutineScope] that wraps the given coroutine [context].
 *
 * WARNING: Unlike the `CoroutineScope()` factory function, if the given [context]
 * does not contain a [Job] element, no default is provided.
 */
fun RawCoroutineScope(context: CoroutineContext): CoroutineScope = ContextScope(context)

// From, https://github.com/Kotlin/kotlinx.coroutines/blob/1.7.3/kotlinx-coroutines-core/common/src/internal/Scopes.kt#L36
private class ContextScope(
	override val coroutineContext: CoroutineContext,
) : CoroutineScope {
	// CoroutineScope is used intentionally for user-friendly representation
	override fun toString(): String = "CoroutineScope(coroutineContext=$coroutineContext)"
}
