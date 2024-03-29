package kokoro.app.ui.swing

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
	context: CoroutineContext = EmptyCoroutineContext,
	title: String = DEFAULT_TITLE,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : BaseWindowFrame(title, gc) {

	@Volatile private var _scope: CoroutineScope = PlaceholderCoroutineScope(context)

	/** NOTE: Used as both a placeholder and a lock object. */
	private class PlaceholderCoroutineScope(
		@JvmField val contextBeforeActualScopeCreated: CoroutineContext
	) : CoroutineScope {
		override val coroutineContext: CoroutineContext
			get() = throw AssertionError("Should be unreachable")
	}

	val scope: CoroutineScope
		get() {
			// NOTE: Laid out similar to JVM implementation of `LazyThreadSafetyMode.SYNCHRONIZED`

			val _v1 = _scope
			if (_v1 !is PlaceholderCoroutineScope) return _v1

			return synchronized(_v1) {
				val _v2 = _scope
				if (_v2 !== _v1) {
					_v2
				} else {
					val _v3 = onCreateScope(_v1.contextBeforeActualScopeCreated)
					_scope = _v3
					_v3
				}
			}
		}

	protected open fun onCreateScope(context: CoroutineContext): CoroutineScope {
		val job = SupervisorJob(context[Job])
		job.invokeOnCompletion { disposePermanently() }
		return RawCoroutineScope(context + super.ref, job)
	}

	// --

	private val _isDisposedPermanently = atomic(false)

	val isDisposedPermanently: Boolean get() = _isDisposedPermanently.value

	open fun disposePermanently() {
		if (_isDisposedPermanently.compareAndSet(expect = false, true)) try {
			onDisposePermanently()
			scope.coroutineContext[Job]?.cancel(null)
		} catch (ex: Throwable) {
			_isDisposedPermanently.value = false
			throw ex
		}
	}

	protected open fun onDisposePermanently() {
		disposeLightly()
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