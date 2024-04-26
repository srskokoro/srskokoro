package kokoro.internal

import kokoro.internal.CleanProcessExit.Hook
import kokoro.internal.CleanProcessExit.Signal
import kokoro.internal.CleanProcessExit.isDoExitNonBlocking
import kokoro.internal.CleanProcessExit.status
import kokoro.internal.CleanProcessExitThread.Companion.EXEC_HOOK_MARK
import kokoro.internal.CleanProcessExitThread.Companion.hooks
import kotlinx.atomicfu.atomic
import java.awt.EventQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport
import kotlin.collections.MutableMap.MutableEntry
import kokoro.internal.CleanProcessExitThread.Companion.isExiting as isExiting_

/**
 * @see exitProcessCleanly
 * @see exitProcessCleanlyLater
 */
object CleanProcessExit {

	init {
		// Reduce the risk of "lost unpark" due to classloading, as recommended
		// by `LockSupport` docs. See also, https://tinyurl.com/JDK-8074773
		LockSupport::class.java
	}

	@Volatile @JvmField var status: Int = 0

	@Suppress("NOTHING_TO_INLINE")
	inline fun status(status: Int): CleanProcessExit {
		this.status = status
		return this
	}

	val isExiting: Boolean get() = isExiting_.value

	/**
	 * @see status
	 * @see exitProcessCleanlyLater
	 * @see doExit
	 * @see doExitNonBlocking
	 */
	@Suppress("NOTHING_TO_INLINE")
	inline fun doExitLater(): Unit = exitThread.start()

	/**
	 * @see status
	 * @see exitProcessCleanlyNonBlocking
	 * @see doExit
	 * @see doExitLater
	 */
	@Suppress("NOTHING_TO_INLINE")
	@JvmName("doExitNonBlocking_")
	inline fun doExitNonBlocking(): Nothing {
		@Suppress("DEPRECATION_ERROR")
		throw doExitNonBlocking_()
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@JvmName("doExitNonBlocking")
	fun doExitNonBlocking_(): Signal {
		doExitLater()
		if (Thread.currentThread() != exitThread) {
			Thread.yield() // Give the exit thread an opportunity
		}
		throw Signal()
	}

	/**
	 * @see status
	 * @see exitProcessCleanly
	 * @see doExitLater
	 * @see doExitNonBlocking
	 * @see isDoExitNonBlocking
	 */
	@Suppress("NOTHING_TO_INLINE")
	@JvmName("doExit_")
	inline fun doExit(): Nothing {
		@Suppress("DEPRECATION_ERROR")
		throw doExit_()
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@JvmName("doExit")
	fun doExit_(): Signal {
		doExitLater()

		run<Unit> {
			if (Thread.currentThread() != exitThread) {
				Thread.yield() // Give the exit thread an opportunity
				if (
					isDoExitNonBlocking.get() != true &&
					!EventQueue.isDispatchThread()
				) return@run // Perform a blocking exit
			}
			throw Signal()
		}

		// NOTE: The following doesn't care about a "lost unpark" -- its goal is
		// to simply "park" indefinitely anyway.
		while (true) {
			LockSupport.park() // Returns when "interrupted status" set
			Thread.interrupted() // Clear and discard "interrupted status"
		}
	}

	/** @see doExit */
	@JvmField val isDoExitNonBlocking = object : ThreadLocal<Boolean>() {
		override fun initialValue(): Boolean = false
	}

	class Signal : Throwable(null, null, true, false) {

		fun throwAnySuppressed() {
			val suppressed = suppressed
			if (suppressed.isEmpty()) return

			val ex = suppressed[0]
			for (i in 1..<suppressed.size) {
				ex.addSuppressed_(suppressed[i])
			}
			if (ex !is Signal) throw ex
			ex.throwAnySuppressed()
		}
	}

	// --

	fun interface Hook {

		fun onCleanup()
	}

	fun addHook(rank: Int, hook: Hook) {
		val x = rank.toLong() and 0xFFFF_FFFF // Ensure non-negative as `Long`
		val rankBox = AtomicLong(x)

		hooks[hook] = rankBox
		if (!isExiting_.value) return // Early return. Skip code below.

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
		if (!isExiting_.value) return // Early return. Skip code below.

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
				assert({ witness < 0 })

				// Check if hook execution is already prevented.
				if (witness != EXEC_HOOK_MARK) return@run // Successfully prevented execution
			}
			// Hook was already marked for execution, i.e., it's too late to
			// remove it. Throw then.
			throw E_HooksAlreadyRunning()
		}
	}

	private fun E_HooksAlreadyRunning() = IllegalStateException("Hooks already running")

	// --

	@JvmField val exitThread: Thread = CleanProcessExitThread()
}

private class CleanProcessExitThread : Thread(
	ROOT_THREAD_GROUP, null,
	CleanProcessExit::class.simpleName,
	0, false,
) {
	companion object {

		const val EXEC_HOOK_MARK = -1L

		@JvmField val hooks = ConcurrentHashMap<Hook, AtomicLong>()

		@JvmField internal val isExiting = atomic(false)

		// --

		@JvmField val ROOT_THREAD_GROUP = run(fun(): ThreadGroup {
			var g: ThreadGroup = currentThread().threadGroup
			while (true) g = g.parent ?: break
			return g
		})
	}

	/**
	 * The [Runtime] shutdown hook for starting [CleanProcessExitThread]
	 * automatically, just in case it won't be started manually.
	 *
	 * NOTE: Prior to the beginning of the [Runtime] shutdown sequence, it is
	 * possible for a program to start a shutdown hook by calling its [Thread.start]
	 * method explicitly. According to the docs, if this occurs, the behavior of
	 * the shutdown sequence is unspecified. Given that [CleanProcessExitThread]
	 * may be started manually, it is wise to have a different thread registered
	 * as a [Runtime] shutdown hook.
	 */
	@JvmField val shutdownHook: Thread

	@Volatile // -- intended to be used with `LockSupport`
	@JvmField var shutdownHook_allowTerminate = false

	init {
		// Ensure "max priority" for when started by a shutdown hook (see below)
		priority = MAX_PRIORITY

		val starter = Runnable {
			this@CleanProcessExitThread.start()

			while (!shutdownHook_allowTerminate) {
				LockSupport.park()
				interrupted() // Discard and ignore interrupts
			}
		}

		val h = ofVirtual()
			.inheritInheritableThreadLocals(false)
			.unstarted(starter)

		shutdownHook = h

		try {
			Runtime.getRuntime().addShutdownHook(h)
		} catch (ex: Throwable) {
			ROOT_THREAD_GROUP.uncaughtException(currentThread(), ex)
		}
	}

	override fun start() {
		if (isExiting.compareAndSet(false, true)) {
			super.start()
		}
	}

	override fun run() {
		val entries = hooks.entries.toTypedArray()

		// Sort by the set `rank` value
		val comparator = Comparator<MutableEntry<Hook, AtomicLong>> { a, b ->
			a.value.get().toInt().compareTo(b.value.get().toInt())
		}
		entries.sortWith(comparator)

		for ((hook, rankBox) in entries) {
			val x = rankBox.get()
			if (x >= 0 && rankBox.compareAndSet(x, EXEC_HOOK_MARK)) try {
				try {
					hook.onCleanup()
				} catch (ex: Signal) {
					ex.throwAnySuppressed()
				}
			} catch (ex: Throwable) {
				try {
					ROOT_THREAD_GROUP.uncaughtException(this, ex)
				} catch (_: Throwable) {
					// Ignore.
				}
			}
		}

		shutdownHook_allowTerminate = true
		LockSupport.unpark(shutdownHook)

		Runtime.getRuntime().exit(status)
	}
}
