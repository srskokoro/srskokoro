package kokoro.internal.system

import kokoro.internal.assert
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.exitProcess

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

	@JvmField @Volatile var statusCode: Int = 0

	@JvmField val THREAD = ExitThread()

	/**
	 * @see statusCode
	 * @see THREAD
	 * @see runBlocking
	 */
	fun run() {
		try {
			THREAD.start()
		} catch (_: IllegalThreadStateException) {
			// Already started
		}
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

	private val BLOCKER = Object()

	fun blockUntilExit(): Nothing {
		while (true) {
			try {
				val o = BLOCKER
				synchronized(o) { o.wait() }
			} catch (_: InterruptedException) {
				// Ignore
			}
		}
	}

	class ExitThread internal constructor() : Thread(
		ROOT_THREAD_GROUP, null,
		CleanProcessExit::class.simpleName,
		0, false,
	) {
		override fun run() {
			isExiting = true

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

			exitProcess(statusCode)
		}

		companion object {
			private val ROOT_THREAD_GROUP = run(fun(): ThreadGroup {
				var g: ThreadGroup = currentThread().threadGroup
				while (true) g = g.parent ?: break
				return g
			})
		}
	}

	// --

	fun interface Hook {

		fun onCleanup()
	}

	private const val EXEC_HOOK_MARK = -1L

	@Volatile private var isExiting = false

	private val hooks = ConcurrentHashMap<Hook, AtomicLong>()

	fun addHook(hook: Hook, rank: Int) {
		val x = rank.toLong() and 0xFFFF_FFFF // Ensure non-negative as `Long`
		val rankBox = AtomicLong(x)

		hooks[hook] = rankBox
		if (!isExiting) return // Early return. Skip code below.

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
		if (!isExiting) return // Early return. Skip code below.

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
