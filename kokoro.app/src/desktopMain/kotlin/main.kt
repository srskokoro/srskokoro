import kokoro.app.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import java.io.File
import java.io.RandomAccessFile

// The `SI_` here stands for "single instance"
private const val SI_QUERY_LOCK_BYTE_POS = 0L
private const val SI_ENFORCEMENT_LOCK_BYTE_POS = 1L

fun main(args: Array<out String>) {
	val lockFile = File(AppData.deviceBoundMain.parent!!.toString(), ".lock")
	// NOTE: Opens the lock file with `RandomAccessFile` so that we get the
	// benefits of `ExtendedOpenOption.NOSHARE_DELETE` (from `com.sun.nio.file`)
	// without either having to check if its available or needing to guard
	// against `UnsupportedOperationException`. Please don't change this to NIO
	// without first seeing, https://stackoverflow.com/a/39298690
	val lockRaf = RandomAccessFile(lockFile, "rw") // May throw; Let it!
	try {
		val lockCh = lockRaf.channel
		lockCh.lock(SI_QUERY_LOCK_BYTE_POS, /*size=*/1, /*shared=*/false).use {
			// NOTE: No need to prevent the following acquired `FileLock` from
			// being GC'ed. The `FileChannel` already keeps a strong reference
			// to it (see `FileLockTable.locks`), while the JVM holds a weak
			// reference to it in order to throw `OverlappingFileLockException`
			// on future overlapping lock attempts.
			if (lockCh.tryLock(SI_ENFORCEMENT_LOCK_BYTE_POS, /*size=*/1, /*shared=*/false) != null) {
				// We're the first instance!
				establishSingleInstance(args)
			} else {
				// We're a secondary instance!
				forwardToSingleInstanceThenExit(args)
			}
		}
	} catch (ex: Throwable) {
		try {
			lockRaf.close()
		} catch (exx: Throwable) {
			ex.addSuppressed(exx)
		}
		throw ex
	}
}

private fun establishSingleInstance(args: Array<out String>): Unit = runBlocking {
	launch(Dispatchers.Swing) {
		run(System.getProperty("user.dir"), args)
	}
}

private fun forwardToSingleInstanceThenExit(args: Array<out String>): Nothing {
	TODO()
}
