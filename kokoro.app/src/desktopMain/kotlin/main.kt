import kokoro.app.AppData
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * An offset to a lock byte that can be locked on in the lock file.
 *
 * This lock byte is what a process should lock on whenever it needs to update
 * the application instance count, add more application instances, designate a
 * particular process as the master instance, etc.
 *
 * @see MASTER_INSTANCE_LOCK_BYTE
 */
private const val INSTANCE_CHANGE_LOCK_BYTE = 0L

/**
 * An offset to a lock byte that can be locked on in the lock file.
 *
 * This lock byte is what a process should lock on in order to designate itself
 * as the master instance of the application's single-program process model.
 *
 * This lock must only be acquired and/or released while locking on [INSTANCE_CHANGE_LOCK_BYTE].
 */
private const val MASTER_INSTANCE_LOCK_BYTE = 1L

fun main(args: Array<out String>) {
	// TODO Install uncaught exception handler

	val lockDir = AppData.deviceBoundMain.parent!!.toString()
	val lockFile = File(lockDir, ".lock")

	// NOTE: Opens the lock file with `RandomAccessFile` so that we get the
	// benefits of `ExtendedOpenOption.NOSHARE_DELETE` (from `com.sun.nio.file`)
	// without either having to check if its available or needing to guard
	// against `UnsupportedOperationException`. Please don't change this to NIO
	// without first seeing, https://stackoverflow.com/a/39298690
	val lockRaf = RandomAccessFile(lockFile, "rw") // May throw; Let it!

	val lockChannel = lockRaf.channel
	try {
		/*
		 * WARNING: DO NOT REORDER THE FOLLOWING LOCKING SEQUENCE without first
		 * understanding the catastrophic consequences of doing so (or why they
		 * were ordered the way they are at the moment).
 		 */
		val instanceChangeLock = lockChannel.lock(INSTANCE_CHANGE_LOCK_BYTE, /*size=*/1, /*shared=*/false)
		val masterInstanceLock = lockChannel.tryLock(MASTER_INSTANCE_LOCK_BYTE, /*size=*/1, /*shared=*/false)

		if (masterInstanceLock != null) {
			// We're the first instance!

			val daemon = AppDaemon(lockDir, lockChannel, masterInstanceLock, args)
			instanceChangeLock.release()

			daemon.doWorkLoop() // Will block the current thread
		} else {
			// We're a secondary instance!
			TODO { NOP }
		}
	} catch (ex: Throwable) {
		lockChannel.closeInCatch(ex) // Releases all locks
		throw ex
	}
}

private class AppDaemon(
	sockDir: String,

	private val lockChannel: FileChannel,
	private val masterInstanceLock: FileLock,

	initialArgs: Array<out String>,
) {
	fun doWorkLoop() {
		TODO { IMPLEMENT }
	}

	// --

	// > 0 - Has current app instances
	//   0 - No current app instances
	// < 0 - Daemon already shut down
	private val appInstanceCount = AtomicInteger()

	private inline fun handleAppInstance(block: () -> Unit) {
		val count = appInstanceCount
		val observed = count.incrementAndGet()
		if (observed > 0) {
			try {
				block()
			} finally {
				if (count.decrementAndGet() == 0) {
					considerShutdown()
				}
			}
		} else {
			revertAppInstanceIncrementAndMaybeFail(observed)
		}
	}

	private fun revertAppInstanceIncrementAndMaybeFail(observedCount: Int) {
		TODO { observedCount }
	}

	private fun considerShutdown() {
		// Blocks until the lock is acquired
		val instanceChangeLock = lockChannel.lock(INSTANCE_CHANGE_LOCK_BYTE, /*size=*/1, /*shared=*/false)
		// NOTE: ^ By the time we've acquired the above lock, the app instance
		// count might have already changed.

		// Double-check and don't proceed if app instances may still run
		if (appInstanceCount.compareAndSet(0, Int.MIN_VALUE)) {
			instanceChangeLock.release() // Done. Nothing should be done.
			return // Skip everything below
		}

		lockChannel.use {
			instanceChangeLock.use {
				masterInstanceLock.use {
					TODO { IMPLEMENT }
				}
			}
		}
	}
}

private fun AutoCloseable.closeInCatch(ex: Throwable) {
	try {
		close()
	} catch (exx: Throwable) {
		ex.addSuppressed(exx)
	}
}
