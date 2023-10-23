package main

import kokoro.app.AppBuild
import kokoro.app.ui.StackTraceModal
import kokoro.internal.DEBUG
import kokoro.internal.closeInCatch
import kokoro.internal.throwAnySuppressed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import main.SingleProcessModel.INSTANCE_CHANGE_LOCK_BYTE
import okio.BufferedSource
import okio.EOFException
import okio.buffer
import okio.source
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger

internal class AppDaemon(
	sockDir: String,
	private val lockChannel: FileChannel,
	private val masterInstanceLock: FileLock,
) {
	private val server: ServerSocketChannel
	private val bindPath: Path

	// > 0 - Has current app instances
	//   0 - No current app instances
	// < 0 - Daemon already shut down
	private val appInstanceCount = AtomicInteger() // Must be set first before the `init` block below

	init {
		val server: ServerSocketChannel
		val bindPath = Path.of(sockDir, ".sock")
		val bindAddress: SocketAddress

		val serverUnix = try {
			ServerSocketChannel.open(StandardProtocolFamily.UNIX)
		} catch (_: UnsupportedOperationException) {
			null
		}
		if (serverUnix != null) {
			server = serverUnix
			bindAddress = UnixDomainSocketAddress.of(bindPath)
		} else {
			server = ServerSocketChannel.open()
			bindAddress = InetSocketAddress(InetAddress.getLoopbackAddress(), 0)
		}
		this.server = server
		this.bindPath = bindPath

		try {
			if (Files.deleteIfExists(bindPath)) {
				// Case: last process didn't shut down cleanly.
				cleanUpLastProcessCrash()
			}
			server.bind(bindAddress)
			if (serverUnix == null) {
				// The now bound server is INET; let everyone know the port.
				generateInetPortFile(bindPath, boundServer = server)
			}
		} catch (ex: Throwable) {
			server.closeInCatch(ex)
			throw ex
		}
	}

	internal object ClientScope : CoroutineScope {
		override val coroutineContext = SupervisorJob() + Dispatchers.Swing + StackTraceModal
	}

	fun doWorkLoop() {
		val scope = ClientScope
		val server = this.server
		try {
			while (true) {
				val client = server.accept() // May throw

				@OptIn(ExperimentalCoroutinesApi::class)
				// The following won't throw here (but may, in a separate coroutine).
				scope.launch(Dispatchers.IO, CoroutineStart.ATOMIC) {
					try {
						handleAppInstance {
							sendVersionCode(client)
							val source = Channels.newInputStream(client).source().buffer()
							val execState = when (val protocol = identifyRelayProtocol(source)) {
								APP_RELAY_PROTOCOL_01.ID -> APP_RELAY_PROTOCOL_01.consume(source)
								else -> throw UnsupportedOperationException(
									"Unknown relay protocol: 0x${protocol.toString(16)} ($protocol)")
							}
							withContext(Dispatchers.Swing) {
								execState.transition()
							}
						}
					} catch (ex: Throwable) {
						client.closeInCatch(ex)
						if (ex is AbortClientConnection) {
							ex.throwAnySuppressed()
						} else throw ex
					}
				}
			}
		} catch (ex: ClosedChannelException) {
			// Daemon shutdown was requested.
			// Do nothing.
		} catch (ex: Throwable) {
			onServerCrashedWhileRunningApp(server, ex)
		}
	}

	/** Called when an app instance has already run and the server crashes. */
	@Suppress("NOTHING_TO_INLINE")
	private inline fun onServerCrashedWhileRunningApp(server: ServerSocketChannel, ex: Throwable): Nothing {
		// Wrap as `Error` to treat it as fatal!
		val err = Error("App daemon server crashed", ex)
		server.closeInCatch(err)
		throw err
	}

	// --

	/**
	 * Custom throwable to simply end the active client connection, without fear
	 * of catching exceptions not related to the processing of an incoming
	 * client. That is, we used a custom exception so that once the app instance
	 * is now running, and the client already closed, we're guaranteed to not
	 * accidentally catch an exception thrown by that app instance's running
	 * logic.
	 */
	class AbortClientConnection : CancellationException() {
		override fun fillInStackTrace(): Throwable = this
	}

	private fun sendVersionCode(client: SocketChannel) {
		val bb = ByteBuffer.allocate(1)
		if (AppBuild.VERSION_CODE > Byte.MAX_VALUE) throw AssertionError(
			"Should be implemented as a varint at this point"
		)
		bb.put(AppBuild.VERSION_CODE.toByte())
		bb.rewind()
		try {
			client.write(bb)
		} catch (ex: IOException) {
			if (DEBUG && ex !is ClosedChannelException) throw ex
			// Ignore the original exception.
			// Simply pretend that the client disconnected early.
			throw AbortClientConnection()
		}
	}

	private fun identifyRelayProtocol(source: BufferedSource): Int {
		try {
			return source.readByte().toInt()
		} catch (ex: IOException) {
			when (ex) {
				is ClosedChannelException, is EOFException -> {
					// Client disconnected early.
					// Perhaps its process got killed.
					// Anyway, abort.
					throw AbortClientConnection()
				}
				else -> throw ex
			}
		}
	}

	// --

	internal inline fun handleAppInstance(block: () -> Unit) {
		val count = appInstanceCount
		val observed = count.incrementAndGet()
		if (observed > 0) {
			try {
				block()
			} catch (ex: Throwable) {
				if (count.decrementAndGet() == 0) try {
					considerShutdown()
				} catch (exx: Throwable) {
					ex.addSuppressed(exx)
				}
				throw ex
			}
			if (count.decrementAndGet() == 0) {
				considerShutdown()
			}
		} else {
			revertAppInstanceIncrementAndMaybeFail(observed)
		}
	}

	private fun revertAppInstanceIncrementAndMaybeFail(observedCount: Int) {
		appInstanceCount.decrementAndGet() // Revert the (presumed) increment
		if (0 > observedCount && observedCount > Int.MIN_VALUE) {
			// Daemon already shut down.
			// Pretend that our process got killed while processing a client.
			// Kill any enclosing coroutine.
			throw CancellationException("Daemon already shut down")
		} else if (observedCount == 0 || observedCount == Int.MIN_VALUE) {
			throw Error("Overflow: maximum app instance count exceeded")
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

		instanceChangeLock.use {
			masterInstanceLock.use {
				server.use {
					Files.deleteIfExists(bindPath)
				}
			}
		}
	}
}

/**
 * Atomically generates a file containing the INET port number as a 2-byte
 * sequence in big-endian byte order.
 *
 * @see readInetPortFile
 */
private fun generateInetPortFile(target: Path, boundServer: ServerSocketChannel) {
	val address = boundServer.localAddress // May throw `ClosedChannelException`
		as InetSocketAddress // Cast throws NPE if the server is not bound
	val port = address.port.toShort()

	val bb = ByteBuffer.allocate(2)
	bb.putShort(port)
	bb.rewind()

	// Output to a temporary file first
	val tmp = Path.of("$target.tmp")

	// Needs `DSYNC` here since otherwise, file writes can be delayed by the OS
	// (even when properly closed) and we have to do a rename/move operation
	// later to atomically publish our changes. See also, https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/package-summary.html#integrity
	FileChannel.open(tmp, StandardOpenOption.DSYNC, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use {
		it.write(bb)
	}

	// Atomically publish our changes via a rename/move operation
	Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
}
