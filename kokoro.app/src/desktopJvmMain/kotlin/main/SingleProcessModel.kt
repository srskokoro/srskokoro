package main

import kokoro.internal.closeInCatch
import main.SingleProcessModel.INSTANCE_CHANGE_LOCK_BYTE
import main.SingleProcessModel.MASTER_INSTANCE_LOCK_BYTE
import main.cli.ClientMain
import main.cli.PrimaryMain
import main.cli.engine.ExecutionState
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel

/**
 * @see setUpSingleProcessModel
 */
internal object SingleProcessModel {

	/**
	 * An offset to a lock byte that can be locked on in the lock file.
	 *
	 * This lock byte is what a process should lock on whenever it needs to
	 * update the application instance count, add more application instances,
	 * designate a particular process as the master instance, etc.
	 *
	 * @see MASTER_INSTANCE_LOCK_BYTE
	 */
	const val INSTANCE_CHANGE_LOCK_BYTE = 0L

	/**
	 * An offset to a lock byte that can be locked on in the lock file.
	 *
	 * This lock byte is what a process should lock on in order to designate
	 * itself as the master instance of the application's single-program process
	 * model.
	 *
	 * This lock must only be acquired and/or released while locking on
	 * [INSTANCE_CHANGE_LOCK_BYTE].
	 */
	const val MASTER_INSTANCE_LOCK_BYTE = 1L
}

/**
 * NOTE: Ideally called in [PrimaryMain.run]`()`
 */
internal fun PrimaryMain.setUpSingleProcessModel() {
	val lockDir = mainDataDir
	val lockFile = File(lockDir, ".lock")

	// NOTE: Opens the lock file with `RandomAccessFile` so that we get the
	// benefits of `ExtendedOpenOption.NOSHARE_DELETE` (from `com.sun.nio.file`)
	// without either having to check if its available or needing to guard
	// against `UnsupportedOperationException`. Please don't change this to NIO
	// without first seeing, https://stackoverflow.com/a/39298690
	val lockRaf = RandomAccessFile(lockFile, "rw") // May throw; let it!
	val lockChannel: FileChannel = lockRaf.channel
	// ^ WARNING: Do NOT close the above `FileChannel` nor the `RandomAccessFile`;
	// let the process exit WITHOUT closing them; let the operating system deal
	// with them on process exit. Any thread may release any of the following
	// `FileLock` objects, and doing so would throw if the `FileChannel` that
	// created the `FileLock` is already closed.

	val instanceChangeLock = lockChannel.lock(INSTANCE_CHANGE_LOCK_BYTE, /*size=*/1, /*shared=*/false)
	try {
		val masterInstanceLock = lockChannel.tryLock(MASTER_INSTANCE_LOCK_BYTE, /*size=*/1, /*shared=*/false)
		if (masterInstanceLock != null) {
			// We're the first instance!

			daemon = AppDaemon(lockDir, lockChannel, masterInstanceLock)
			instanceChangeLock.release()

			return // Done. Skip code below.
		} else {
			// We're a secondary instance!

			val relay = AppRelay(lockDir)
			instanceChangeLock.release()

			relay.doForward(execState.args)
			throw ExitMain() // Do a special exit.
		}
	} catch (ex: Throwable) {
		instanceChangeLock.closeInCatch(ex)
		throw ex
	}
}

internal fun establishClientMain(workingDir: String, args: Array<out String>): ExecutionState {
	return ClientMain().feed(workingDir, args)
}
