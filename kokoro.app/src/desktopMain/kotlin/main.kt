import kokoro.app.AppBuild
import kokoro.app.AppData
import kokoro.internal.kotlin.TODO
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.io.File
import java.io.RandomAccessFile
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.SwingUtilities
import kotlin.system.exitProcess
import java.nio.file.Path as NioPath

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

			val relay = AppRelay(lockDir)
			instanceChangeLock.release()

			// Exits the process in either the current thread or Swing EDT
			relay.doForwardAndExit(args)
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
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Swing)

	private val server: ServerSocketChannel
	private val bindPath: NioPath

	// > 0 - Has current app instances
	//   0 - No current app instances
	// < 0 - Daemon already shut down
	private val appInstanceCount = AtomicInteger() // Must be set first before the `init` block below

	init {
		val server: ServerSocketChannel
		val bindPath: NioPath
		val bindAddress: SocketAddress

		val serverUnix = try {
			ServerSocketChannel.open(StandardProtocolFamily.UNIX)
		} catch (_: UnsupportedOperationException) {
			null
		}
		if (serverUnix != null) {
			server = serverUnix
			bindPath = NioPath.of(sockDir, ".sock")
			bindAddress = UnixDomainSocketAddress.of(bindPath)
		} else {
			server = ServerSocketChannel.open()
			bindPath = NioPath.of(sockDir, ".port")
			bindAddress = InetSocketAddress(InetAddress.getLoopbackAddress(), 0)
		}
		this.server = server
		this.bindPath = bindPath

		try {
			if (Files.deleteIfExists(bindPath)) {
				TODO // TODO Do additional cleanup work here
			}
		} catch (ex: Throwable) {
			server.closeInCatch(ex)
			throw ex
		}

		// The following won't throw here (but may, in a separate coroutine).
		scope.launch {
			handleAppInstance {
				// TODO Consume initial args
			}
		}

		try {
			server.bind(bindAddress)
		} catch (ex: Throwable) {
			server.closeInCatch(ex)
			throw ex
		}
		if (serverUnix == null) {
			// Now that our (INET) server is bound, let everyone know the port.
			generateInetPortFile(bindPath, boundServer = server)
		}
	}

	fun doWorkLoop() {
		val scope = this.scope
		val server = this.server
		try {
			while (true) {
				val client = server.accept() // May throw

				@OptIn(ExperimentalCoroutinesApi::class)
				// The following won't throw here (but may, in a separate coroutine).
				scope.launch(Dispatchers.IO, CoroutineStart.ATOMIC) {
					client.use {
						handleAppInstance {
							TODO { IMPLEMENT }
						}
					}
				}
			}
		} catch (ex: ClosedChannelException) {
			// Do nothing. Daemon shutdown was requested.
		} catch (ex: Throwable) {
			server.closeInCatch(ex)
			throw ex
		}
	}

	// --

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
		appInstanceCount.decrementAndGet() // Revert the (presumed) increment
		if (observedCount < 0) {
			return // Daemon already shut down
		} else if (observedCount == 0 || observedCount == Int.MIN_VALUE) {
			throw Error("Maximum app instance count exceeded")
		} else
			throw AssertionError("Unreachable")
	}

	private fun considerShutdown() {
		// Blocks until the lock is acquired
		val instanceChangeLock = lockChannel.lock(INSTANCE_CHANGE_LOCK_BYTE, /*size=*/1, /*shared=*/false)
		// NOTE: ^ By the time we've acquired the above lock, the app instance
		// count might have already changed.

		// Double-check and don't proceed if app instances may still run
		if (!appInstanceCount.compareAndSet(0, Int.MIN_VALUE)) {
			instanceChangeLock.release() // Done. Nothing should be done.
			return // Skip everything below
		}

		lockChannel.use {
			instanceChangeLock.use {
				masterInstanceLock.use {
					server.use {
						Files.deleteIfExists(bindPath)
					}
				}
			}
		}
	}
}

private class AppRelay(sockDir: String) {
	private val client: SocketChannel
	private val serverVersionCode: Int

	init {
		val client: SocketChannel
		val connectAddress: SocketAddress

		val clientUnix = try {
			SocketChannel.open(StandardProtocolFamily.UNIX)
		} catch (_: UnsupportedOperationException) {
			null
		}
		if (clientUnix != null) {
			client = clientUnix
			connectAddress = UnixDomainSocketAddress.of(NioPath.of(sockDir, ".sock"))
		} else {
			client = SocketChannel.open()
			val port = readInetPortFile(NioPath.of(sockDir, ".port"), client)
			connectAddress = InetSocketAddress(InetAddress.getLoopbackAddress(), port)
		}

		this.client = client
		this.serverVersionCode = try {
			client.connect(connectAddress)
			val bb = ByteBuffer.allocate(1)
			if (client.read(bb) > 0) {
				bb.rewind()
				// We don't support versions > 127, for now -- we'll use a varint later.
				bb.get().toInt() // Also, 0 is reserved as a special value.
			} else 0
		} catch (ex: Throwable) {
			client.closeInCatch(ex)
			throw ex
		}
	}

	fun doForwardAndExit(args: Array<out String>) {
		if (serverVersionCode == AppBuild.VERSION_CODE) try {
			TODO { IMPLEMENT }
		} catch (ex: Throwable) {
			client.closeInCatch(ex)
			throw ex
		} else {
			var thrownByClose: Throwable? = null
			try {
				client.close()
			} catch (ex: Throwable) {
				thrownByClose = ex
			}
			SwingUtilities.invokeAndWait {
				// TODO Display error dialog for incompatible version.
				//  - Also, interpret 0 as an unknown error.
				// TODO Display stacktrace dialog for `thrownByClose` if nonnull
				exitProcess(1)
			}
		}
	}
}

// --

private fun generateInetPortFile(target: NioPath, boundServer: ServerSocketChannel) {
	try {
		// The following cast throws NPE if the server is not bound
		val address = boundServer.localAddress as InetSocketAddress
		val port = address.port.toShort()

		val bb = ByteBuffer.allocate(2)
		bb.putShort(port)
		bb.rewind()

		// Output to a temporary file first
		val tmp = NioPath.of("$target.tmp")

		// Needs `DSYNC` here since otherwise, file writes can be delayed by the
		// OS (even when properly closed) and we have to do a rename/move
		// operation later to atomically publish our changes. See also,
		// https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/package-summary.html#integrity
		FileChannel.open(tmp, DSYNC, CREATE, WRITE, TRUNCATE_EXISTING).use {
			it.write(bb)
		}

		// Atomically publish our changes via a rename/move operation
		Files.move(tmp, target, ATOMIC_MOVE, REPLACE_EXISTING)
		// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
	} catch (ex: Throwable) {
		boundServer.closeInCatch(ex)
		throw ex
	}
}

fun readInetPortFile(target: NioPath, client: SocketChannel): Int {
	try {
		val bb = ByteBuffer.allocate(2)
		FileChannel.open(target, READ).use {
			it.read(bb)
		}
		// Flip then get, to throw if not enough bytes read.
		return bb.flip().short.toInt() and 0xFFFF
	} catch (ex: Throwable) {
		client.closeInCatch(ex)
		throw ex
	}
}

private fun AutoCloseable.closeInCatch(ex: Throwable) {
	try {
		close()
	} catch (exx: Throwable) {
		ex.addSuppressed(exx)
	}
}
