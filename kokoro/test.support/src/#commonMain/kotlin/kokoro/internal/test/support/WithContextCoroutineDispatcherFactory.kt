package kokoro.internal.test.support

import io.kotest.common.ExperimentalKotest
import io.kotest.core.concurrency.CoroutineDispatcherFactory
import io.kotest.core.config.ProjectConfiguration
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalKotest::class)
inline fun Spec.withConcurrencyVia(context: CoroutineContext, concurrency: Int = ProjectConfiguration.MaxConcurrency) {
	this.concurrency = concurrency
	this.coroutineDispatcherFactory = CoroutineDispatcherFactory(context)
}

/** Alias for [WithContextCoroutineDispatcherFactory] construction. */
@Suppress("NOTHING_TO_INLINE")
inline fun CoroutineDispatcherFactory(context: CoroutineContext) = WithContextCoroutineDispatcherFactory(context)

class WithContextCoroutineDispatcherFactory(private val context: CoroutineContext) : CoroutineDispatcherFactory {
	override suspend fun <T> withDispatcher(testCase: TestCase, f: suspend () -> T): T {
		// Switches context and continues from there
		return withContext(context) { f() }
	}
}
