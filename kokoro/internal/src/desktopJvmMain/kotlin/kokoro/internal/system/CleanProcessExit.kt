package kokoro.internal.system

import kokoro.internal.assert
import kokoro.internal.system.CleanProcessExit.EXEC_HOOK_MARK
import kokoro.internal.system.CleanProcessExit._isExiting
import kokoro.internal.system.CleanProcessExit.hooks
import kokoro.internal.system.CleanProcessExit.shutdownHook
import kokoro.internal.system.CleanProcessExit.shutdownHook_allowTerminate
import kokoro.internal.system.CleanProcessExit.statusCode
import kokoro.internal.system.CleanProcessExitThread.Companion.ROOT_THREAD_GROUP
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport

@Suppress("NOTHING_TO_INLINE")
inline fun cleanProcessExit() {
	CleanProcessExit.run()
}

@Suppress("NOTHING_TO_INLINE")
inline fun cleanProcessExitBlocking(): Nothing {
	CleanProcessExit.runBlocking()
}

@Suppress("NOTHING_TO_INLINE")
inline fun cleanProcessExit(statusCode: Int) {
	CleanProcessExit.statusCode = statusCode
	CleanProcessExit.run()
}

@Suppress("NOTHING_TO_INLINE")
inline fun cleanProcessExitBlocking(statusCode: Int): Nothing {
	CleanProcessExit.statusCode = statusCode
	CleanProcessExit.runBlocking()
}

object CleanProcessExit {

	@Volatile @JvmField var statusCode: Int = 0

	@JvmField internal val _isExiting = AtomicBoolean(false)

	val isExiting: Boolean get() = _isExiting.get()

	@JvmField val THREAD: CleanProcessExitThread

	init {
		val t = CleanProcessExitThread()
		THREAD = t

		// Ensure "max priority" for when started by a shutdown hook (see below)
		t.priority = Thread.MAX_PRIORITY
	}

	/**
	 * @see statusCode
	 * @see THREAD
	 * @see runBlocking
	 */
	@Suppress("NOTHING_TO_INLINE")
	inline fun run() {
		THREAD.start()
	}

	/**
	 * @see statusCode
	 * @see THREAD
	 * @see run
	 */
	@Suppress("NOTHING_TO_INLINE")
	inline fun runBlocking(): Nothing {
		run()
		blockUntilExit()
	}

	fun blockUntilExit(): Nothing {
		while (true) {
			try {
				@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
				val o = _isExiting as Object
				synchronized(o) { o.wait() }
			} catch (_: InterruptedException) {
				// Ignore
			}
		}
	}

	// --

	/**
	 * The `Runtime` shutdown hook for starting [THREAD], just in case [THREAD]
	 * won't be started manually.
	 */
	@JvmField internal val shutdownHook: Thread

	@Volatile // -- intended to be used with `LockSupport`
	@JvmField internal var shutdownHook_allowTerminate = false

	init {
		// Reduce the risk of "lost unpark" due to classloading, as recommended
		// by `LockSupport` docs. See also, https://bugs.openjdk.org/browse/JDK-8074773?focusedId=13621169#comment-13621169
		LockSupport::class.java
	}

	init {
		val starter = Runnable {
			THREAD.start()

			while (!shutdownHook_allowTerminate)
				LockSupport.park()
		}

		val h = Thread.ofVirtual()
			.inheritInheritableThreadLocals(false)
			.unstarted(starter)

		shutdownHook = h

		try {
			Runtime.getRuntime().addShutdownHook(h)
		} catch (ex: Throwable) {
			ROOT_THREAD_GROUP.uncaughtException(Thread.currentThread(), ex)
		}
	}

	// --

	fun interface Hook {

		fun onCleanup()
	}

	internal const val EXEC_HOOK_MARK = -1L

	@JvmField internal val hooks = ConcurrentHashMap<Hook, AtomicLong>()

	fun addHook(rank: Int, hook: Hook) {
		val x = rank.toLong() and 0xFFFF_FFFF // Ensure non-negative as `Long`
		val rankBox = AtomicLong(x)

		hooks[hook] = rankBox
		if (!_isExiting.get()) return // Early return. Skip code below.

		// The primary purpose of the enclosing function is to ensure that the
		// given hook will execute eventually. However, the execution of hooks
		// has already begun.

		// OBJECTIVES: If the given hook is not yet marked for execution, try to
		// snatch it. If it's already marked for execution, we won't be able to
		// snatch it. If we managed to snatch it, then it's either left behind
		// already (and won't be executing) or it'll be left behind (as we've
		// managed snatched it). If the hook won't be executing, throw -- as we
		// failed to fulfill the primary purpose of the enclosing function.

		// ASSUMPTION: No one else will ever set `rankBox` with a differing
		// non-negative value. Others will only ever set `hooks[hook]`, which
		// replaces `rankBox` (and leaves `rankBox` unaltered).
		if (rankBox.compareAndSet(x, x or (1L shl 63)) || rankBox.get() != EXEC_HOOK_MARK) {
			// Case: value successfully stolen or already stolen, so as to
			// prevent the hook from executing.
			throw E_HooksAlreadyRunning() // Throw as it won't be executing now.
		}
	}

	fun removeHook(hook: Hook) {
		val rankBox = hooks.remove(hook)
		if (!_isExiting.get()) return // Early return. Skip code below.

		// The primary purpose of the enclosing function is to prevent the hook
		// from executing. However, the execution of hooks has already begun.

		// OBJECTIVES: If the hook is already marked for execution, throw.
		// Otherwise, attempt to prevent its execution; and if we managed to do
		// just that, return normally -- as we've successfully fulfilled the
		// primary purpose of the enclosing function.

		if (rankBox != null) run<Unit> {
			val x = rankBox.get()
			if (x != EXEC_HOOK_MARK) {
				// Attempt to prevent hook execution (if not already prevented).
				val witness = rankBox.compareAndExchange(x, x or (1L shl 63))
				if (witness == x) return@run // Successfully prevented execution

				// ASSUMPTION: The initial value of `rankBox` is always
				// non-negative, and further updates to `rankBox` will only ever
				// set it to a negative value, so as to either mark the hook for
				// execution or prevent the hook's execution. Others will only
				// ever set `hooks[hook]`, which replaces `rankBox` (but leaves
				// `rankBox` unaltered).
				assert { witness < 0 }

				// Check if hook execution is already prevented.
				if (witness != EXEC_HOOK_MARK) return@run // Successfully prevented execution
			}
			// Hook was already marked for execution, i.e., it's too late to
			// remove it. Throw then.
			throw E_HooksAlreadyRunning()
		}
	}

	private fun E_HooksAlreadyRunning() = IllegalStateException("Hooks already running")
}

class CleanProcessExitThread internal constructor() : Thread(
	ROOT_THREAD_GROUP, null,
	CleanProcessExit::class.simpleName,
	0, false,
) {
	override fun start() {
		if (_isExiting.compareAndSet(false, true)) {
			super.start()
		}
	}

	override fun run() {
		val entries = hooks.entries.toTypedArray()
		// Sort by the set `rank` value
		entries.sortBy { it.value.get().toInt() }

		for ((hook, rankBox) in entries) {
			val x = rankBox.get()
			if (x >= 0 && rankBox.compareAndSet(x, EXEC_HOOK_MARK)) try {
				hook.onCleanup()
			} catch (ex: Throwable) {
				ROOT_THREAD_GROUP.uncaughtException(this, ex)
			}
		}

		shutdownHook_allowTerminate = true
		LockSupport.unpark(shutdownHook)

		Runtime.getRuntime().exit(statusCode)
	}

	companion object {
		@JvmField internal val ROOT_THREAD_GROUP = run(fun(): ThreadGroup {
			var g: ThreadGroup = currentThread().threadGroup
			while (true) g = g.parent ?: break
			return g
		})
	}
}
