package kokoro.app.ui

import kokoro.internal.coroutines.RawCoroutineScope
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.awt.GraphicsConfiguration
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
open class ScopedWindowFrame @JvmOverloads constructor(
	context: CoroutineContext = DEFAULT_CONTEXT,
	title: String = DEFAULT_TITLE,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
	scopeFactory: ScopeFactory = DEFAULT_SCOPE_FACTORY,
) : BaseWindowFrame(title, gc) {

	companion object {
		inline val DEFAULT_CONTEXT get() = EmptyCoroutineContext
		inline val DEFAULT_SCOPE_FACTORY: ScopeFactory get() = SupervisorScopeFactory
	}

	fun interface ScopeFactory {
		fun onCreateScope(context: CoroutineContext): CoroutineScope
	}

	object SupervisorScopeFactory : ScopeFactory {
		override fun onCreateScope(context: CoroutineContext) =
			RawCoroutineScope(context, SupervisorJob(context[Job]))
	}

	val scope = @Suppress("LeakingThis") onCreateScope(scopeFactory, context)

	protected open fun onCreateScope(scopeFactory: ScopeFactory, context: CoroutineContext): CoroutineScope {
		return scopeFactory.onCreateScope(context)
	}

	// --

	private val _isDisposedPermanently = atomic(false)

	val isDisposedPermanently: Boolean get() = _isDisposedPermanently.value

	fun disposePermanently() {
		if (_isDisposedPermanently.compareAndSet(expect = false, true)) {
			disposeLightly()
			scope.coroutineContext[Job]?.run { cancel(null) }
		}
	}

	fun disposeLightly() = super.dispose()

	// --

	override fun dispose() = disposePermanently()

	override fun addNotify() {
		if (_isDisposedPermanently.value) {
			throw E_AlreadyDisposedPermanently()
		}
		super.addNotify()
		// Check again in case of race
		if (_isDisposedPermanently.value) {
			super.dispose() // Undo `addNotify()`
			throw E_AlreadyDisposedPermanently()
		}
	}

	private fun E_AlreadyDisposedPermanently() = IllegalStateException("Already disposed (permanently)")
}
