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
) : BaseWindowFrame(title, gc) {

	companion object {
		inline val DEFAULT_CONTEXT get() = EmptyCoroutineContext
	}

	private var contextBeforeScopeCreated: CoroutineContext? = context
	@Volatile private var _scope: CoroutineScope? = null

	val scope: CoroutineScope
		get() {
			// NOTE: Laid out similar to JVM implementation of `LazyThreadSafetyMode.SYNCHRONIZED`

			val _v1 = _scope
			if (_v1 != null) return _v1

			return synchronized(this) {
				val _v2 = _scope
				if (_v2 != null) {
					_v2
				} else {
					val _v3 = onCreateScope(contextBeforeScopeCreated!!)
					contextBeforeScopeCreated = null
					_scope = _v3
					_v3
				}
			}
		}

	protected open fun onCreateScope(context: CoroutineContext): CoroutineScope {
		return RawCoroutineScope(context, SupervisorJob(context[Job]))
	}

	// --

	private val _isDisposedPermanently = atomic(false)

	val isDisposedPermanently: Boolean get() = _isDisposedPermanently.value

	open fun disposePermanently() {
		if (_isDisposedPermanently.compareAndSet(expect = false, true)) {
			onDisposePermanently()
		}
	}

	protected open fun onDisposePermanently() {
		disposeLightly()
		scope.coroutineContext[Job]?.run { cancel(null) }
	}

	open fun disposeLightly() = super.dispose()

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
}

private fun E_AlreadyDisposedPermanently() = IllegalStateException("Already disposed (permanently)")
