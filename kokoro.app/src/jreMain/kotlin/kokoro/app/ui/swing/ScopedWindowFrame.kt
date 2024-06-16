package kokoro.app.ui.swing

import kokoro.internal.assert
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
			// NOTE: Getting `scope` first ensures that `onCreateScope()` is
			// called first, before the call to `onDisposePermanently()` below.
			scope.coroutineContext[Job]?.cancel(null)
			onDisposePermanently()
		} catch (ex: Throwable) {
			_isDisposedPermanently.value = false
			throw ex
		}
	}

	protected open fun onDisposePermanently(): Unit = disposeLightly()

	/** @see removeNotify */
	fun disposeLightly(): Unit = super.dispose()

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

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun checkNotDisposedPermanently() {
		if (isDisposedPermanently) throw E_AlreadyDisposedPermanently()
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun assertNotDisposedPermanently() {
		assert({ !isDisposedPermanently }) { E_AlreadyDisposedPermanently }
	}

	companion object {
		const val E_AlreadyDisposedPermanently = "Already disposed (permanently)"
		fun E_AlreadyDisposedPermanently() = IllegalStateException(E_AlreadyDisposedPermanently)
	}
}
