package main

import assertUnreachable
import kokoro.app.AppBuild
import kokoro.app.AppData
import kokoro.app.cli.Main
import kokoro.app.ui.Alerts
import kokoro.app.ui.ExitProcessNonZeroViaSwing
import kokoro.app.ui.StackTraceModal
import kokoro.app.ui.ifChoiceMatches
import kokoro.app.ui.swing
import kokoro.internal.DEBUG
import kokoro.jcef.Jcef
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import okio.BufferedSource
import okio.EOFException
import okio.buffer
import okio.sink
import okio.source
import java.awt.EventQueue
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.atomic.AtomicInteger
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

fun main(args: Array<out String>) = runSingleProcessModel(args, object : AppDispatcher {
	override suspend fun dispatch(scope: CoroutineScope, workingDir: String, args: Array<out String>, isInit: Boolean) {
		Main(isInit).feed(workingDir, args, scope)
	}
})

internal fun runSingleProcessModel(args: Array<out String>, dispatcher: AppDispatcher) {
	Thread.setDefaultUncaughtExceptionHandler(StackTraceModal) // Installed early to help with debugging

	val lockDir = AppData.deviceBoundMain.parent!!.toString()
	val lockFile = File(lockDir, ".lock")

	// NOTE: Opens the lock file with `RandomAccessFile` so that we get the
	// benefits of `ExtendedOpenOption.NOSHARE_DELETE` (from `com.sun.nio.file`)
	// without either having to check if its available or needing to guard
	// against `UnsupportedOperationException`. Please don't change this to NIO
	// without first seeing, https://stackoverflow.com/a/39298690
	val lockRaf = RandomAccessFile(lockFile, "rw") // May throw; let it!
	val lockChannel = lockRaf.channel
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

			val daemon = AppDaemon(lockDir, lockChannel, masterInstanceLock, args, dispatcher)
			instanceChangeLock.release()

			daemon.doWorkLoop() // Will block the current thread
		} else {
			// We're a secondary instance!

			val relay = AppRelay(lockDir)
			instanceChangeLock.release()

			relay.doForward(args)
		}
		return // Done!
	} catch (ex: AppRelay.ExitMain) {
		instanceChangeLock.close()
		// Done. Do nothing else.
	} catch (ex: Throwable) {
		instanceChangeLock.closeInCatch(ex)
		throw ex
	}
}

// --

private const val CLI_PROTOCOL_01 = 0x01
private const val CLI_PROTOCOL_DEFAULT = CLI_PROTOCOL_01

private object ClientHandlingSwingScope : CoroutineScope {
	override val coroutineContext = SupervisorJob() + Dispatchers.Swing + StackTraceModal
}

internal fun interface AppDispatcher {
	suspend fun dispatch(scope: CoroutineScope, workingDir: String, args: Array<out String>, isInit: Boolean)
}

private class AppDaemon(
	sockDir: String,

	private val lockChannel: FileChannel,
	private val masterInstanceLock: FileLock,

	initialArgs: Array<out String>,

	private val dispatcher: AppDispatcher,
) {
	private val server: ServerSocketChannel
	private val bindPath: NioPath

	// > 0 - Has current app instances
	//   0 - No current app instances
	// < 0 - Daemon already shut down
	private val appInstanceCount = AtomicInteger() // Must be set first before the `init` block below

	init {
		val server: ServerSocketChannel
		val bindPath = NioPath.of(sockDir, ".sock")
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
			// Cleanup for when our last process didn't shut down cleanly
			if (Files.deleteIfExists(bindPath)) {
				// Some JCEF helpers might have leaked from the last process
				Jcef.killExtraneousJcefHelpers()

				// NOTE: Do additional cleanup work here.
			}
		} catch (ex: Throwable) {
			server.closeInCatch(ex)
			throw ex
		}

		// The following won't throw here (but may, in a separate coroutine).
		ClientHandlingSwingScope.launch {
			handleAppInstance {
				coroutineScope {
					dispatcher.dispatch(this, System.getProperty("user.dir"), initialArgs, isInit = true)
				}
			}
		}

		try {
			server.bind(bindAddress)
			if (serverUnix == null) {
				// The now bound server is INET; let everyone know the port.
				generateInetPortFile(bindPath, boundServer = server)
			}
		} catch (ex: ClosedChannelException) {
			// Daemon shutdown was requested (by the initial app instance).
			// Do nothing.
		} catch (ex: Throwable) {
			onServerCrashedWhileRunningApp(server, ex)
		}
	}

	fun doWorkLoop() {
		val scope = ClientHandlingSwingScope
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
							when (val protocol = identifyCliProtocol(source)) {
								CLI_PROTOCOL_01 -> CLI_PROTOCOL_01_impl(source)
								else -> throw UnsupportedOperationException(
									"Unknown CLI protocol: 0x${protocol.toString(16)} ($protocol)")
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
	private class AbortClientConnection : CancellationException() {
		override fun fillInStackTrace(): Throwable = this

		fun throwAnySuppressed() {
			val suppressed = suppressed
			if (suppressed.isEmpty()) return

			val ex = suppressed[0]
			for (i in 1..<suppressed.size) {
				ex.addSuppressed(suppressed[i])
			}
			throw ex
		}
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

	private fun identifyCliProtocol(source: BufferedSource): Int {
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

	private suspend fun CLI_PROTOCOL_01_impl(source: BufferedSource) {
		val workingDir: String
		val args: Array<String>

		try {
			val payloadCount = source.readInt()
			if (payloadCount > 0) {
				val argsSize = payloadCount - 1
				@Suppress("UNCHECKED_CAST")
				args = arrayOfNulls<String>(argsSize) as Array<String>

				val workingDirLength = source.readInt()
				val argsLengths = IntArray(argsSize)
				repeat(argsSize) { i ->
					argsLengths[i] = source.readInt()
				}

				workingDir = source.readUtf8(workingDirLength.toLong())
				repeat(argsSize) { i ->
					args[i] = source.readUtf8(argsLengths[i].toLong())
				}
			} else {
				workingDir = ""
				args = emptyArray()
			}
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

		@Suppress("BlockingMethodInNonBlockingContext")
		// Close the client connection early, as we might be about to run for a
		// very long time. Doing so may throw -- let it!
		source.close()

		withContext(Dispatchers.Swing) {
			dispatcher.dispatch(this, workingDir, args, isInit = false)
		}
	}

	// --

	private inline fun handleAppInstance(block: () -> Unit) {
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

private class AppRelay(sockDir: String) {
	private val client: SocketChannel
	private val serverVersionCode: Int

	init {
		val client: SocketChannel
		val connectAddress: SocketAddress

		val sockPath = NioPath.of(sockDir, ".sock")
		if (isLikelyUnixSock(sockPath)) {
			try {
				client = SocketChannel.open(StandardProtocolFamily.UNIX)
			} catch (ex: UnsupportedOperationException) {
				// EDGE CASE: The feature to create unix domain sockets could be
				// disabled, and yet, the said feature was enabled when the app
				// daemon was run :P
				showErrorThenExit(null, E_UNIX_SOCK_REQUIRED, ex)
			}
			connectAddress = UnixDomainSocketAddress.of(sockPath)
		} else {
			// Either the daemon didn't use unix domain sockets or the file
			// doesn't exist (or no longer exists). Assume the former, for now.
			val port = try {
				readInetPortFile(sockPath) // Throws if the file doesn't exist
			} catch (ex: IOException) {
				if (Files.exists(sockPath)) throw ex
				// Either the app daemon has (properly) shut down, or someone
				// deleted the file. Show an error assuming the former case.
				showErrorThenExit(null, E_SERVICE_HALT, ex)
			}
			connectAddress = InetSocketAddress(InetAddress.getLoopbackAddress(), port)
			client = SocketChannel.open()
		}

		this.client = client
		this.serverVersionCode = try {
			client.connect(connectAddress)
			val bb = ByteBuffer.allocate(1)
			if (client.read(bb) > 0) {
				bb.rewind()
				// We don't support versions > 127, for now -- we'll use a
				// varint later, where the first byte is always > 127 to
				// indicate the start of a multi-byte varint sequence.
				bb.get().toInt() // Also, 0 is reserved as a special value.
			} else 0
		} catch (ex: IOException) {
			// Will close the client connection for us
			showErrorThenExit(client, E_SERVICE_HALT, ex)
		} catch (ex: Throwable) {
			client.closeInCatch(ex)
			throw ex
		}
	}

	fun doForward(args: Array<out String>) {
		val version = serverVersionCode
		if (version == AppBuild.VERSION_CODE) try {
			val buffer = okio.Buffer()
			if (CLI_PROTOCOL_DEFAULT > Byte.MAX_VALUE) throw AssertionError(
				"Should be implemented as a varint at this point"
			)
			buffer.writeByte(CLI_PROTOCOL_DEFAULT)

			// We'll pass the current working directory plus command arguments
			val payloadCount = 1 + args.size
			buffer.writeInt(payloadCount)

			// Reserves a header for the UTF-8 lengths
			val payloadUtf8LengthsOffset = buffer.size
			var size = payloadUtf8LengthsOffset + Int.SIZE_BYTES * payloadCount

			val unsafe = okio.Buffer.UnsafeCursor()
			buffer.readAndWriteUnsafe(unsafe).use { u ->
				// Needed to ensure a contiguous range of available bytes.
				// - Throws if larger than the buffer's supported segment size.
				u.expandBuffer(Math.toIntExact(size - payloadUtf8LengthsOffset))
				// Resize into where we only plan to write our header
				u.resizeBuffer(size)
			}

			val payloadUtf8Lengths = IntArray(payloadCount)
			var i = 0

			try {
				val currentDir = System.getProperty("user.dir")
				buffer.writeUtf8(currentDir).size.let { newSize ->
					payloadUtf8Lengths[i] = Math.toIntExact(newSize - size)
					size = newSize
				}
				for (arg in args) buffer.writeUtf8(arg).size.let { newSize ->
					payloadUtf8Lengths[++i] = Math.toIntExact(newSize - size)
					size = newSize
				}
			} catch (ex: ArithmeticException) {
				throw IOException(
					(if (i == 0) "Current working directory too long."
					else "Command argument too long (at index ${i - 1}).") +
						" Length in UTF-8 must be less than " + Short.MAX_VALUE +
						" bytes.", ex)
			}

			// Fill the reserved header with the UTF-8 lengths
			buffer.readAndWriteUnsafe(unsafe).use { u ->
				u.seek(payloadUtf8LengthsOffset)
				// Not sure if a loop would be more efficient. Anyway…
				ByteBuffer.wrap(u.data, u.start, u.end - u.start)
					.asIntBuffer().put(payloadUtf8Lengths)
			}

			val sink = Channels.newOutputStream(client).sink()
			try {
				sink.write(buffer, size) // Send it!
			} catch (ex: IOException) {
				// Will close the client connection for us
				showErrorThenExit(sink, E_SERVICE_HALT, ex)
			}
			// Deliberately closing outside of any `try` or `use`, as `close()`
			// may throw and interfere with our custom error handling.
			sink.close() // May throw; let it!
			return // Done!
		} catch (ex: Throwable) {
			client.closeInCatch(ex)
			throw ex
		} else {
			showErrorThenExit(client, if (version >= 0) version else E_VERSION_BEYOND, null)
		}
	}

	private companion object {
		const val E_VERSION_BEYOND = -1
		const val E_SERVICE_HALT = -2
		const val E_UNIX_SOCK_REQUIRED = -3

		private fun showErrorThenExit(client: AutoCloseable?, versionOrErrorCode: Int, cause: Throwable?): Nothing {
			var thrownByClose: Throwable? = null
			try {
				client?.close()
			} catch (ex: Throwable) {
				thrownByClose = ex
			}

			EventQueue.invokeLater {
				Alerts.swing(null) {
					when (versionOrErrorCode) {
						E_SERVICE_HALT -> "Service halt" to "The application " +
							"service terminated before it could process the " +
							"request."

						E_UNIX_SOCK_REQUIRED -> "Incompatible IPC protocol" to

							"Unix domain socket support is required, since the " +
							"first instance of the app was run with it enabled " +
							"(for IPC).\n\n" +

							"This error can be avoided by either re-enabling " +
							"unix domain sockets support or killing all " +
							"instances of the app \u2013 simply restart them as " +
							"needed."

						0 -> "Unknown error" to "Service request failed due to " +
							"an unknown error."

						else -> "Version conflict" to "Cannot proceed while a " +
							"different version of the app is already running."
					}.let { (title, message) ->
						this.title = title
						this.message = message
					}
					style { ERROR }
					buttons { if (cause != null) OK(null, "Details") else OK }
				}.ifChoiceMatches({ CustomAction }) {
					if (cause != null) {
						StackTraceModal.print(cause)
					} else {
						assertUnreachable()
					}
				}

				thrownByClose?.let {
					StackTraceModal.print(it)
				}

				ExitProcessNonZeroViaSwing.install()
			}

			// Done!
			throw ExitMain()
		}
	}

	class ExitMain : Throwable(null, null, false, false)
}

// --

/**
 * Atomically generates a file containing the INET port number as a 2-byte
 * sequence in big-endian byte order.
 */
private fun generateInetPortFile(target: NioPath, boundServer: ServerSocketChannel) {
	val address = boundServer.localAddress // May throw `ClosedChannelException`
		as InetSocketAddress // Cast throws NPE if the server is not bound
	val port = address.port.toShort()

	val bb = ByteBuffer.allocate(2)
	bb.putShort(port)
	bb.rewind()

	// Output to a temporary file first
	val tmp = NioPath.of("$target.tmp")

	// Needs `DSYNC` here since otherwise, file writes can be delayed by the OS
	// (even when properly closed) and we have to do a rename/move operation
	// later to atomically publish our changes. See also, https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/package-summary.html#integrity
	FileChannel.open(tmp, DSYNC, CREATE, WRITE, TRUNCATE_EXISTING).use {
		it.write(bb)
	}

	// Atomically publish our changes via a rename/move operation
	Files.move(tmp, target, ATOMIC_MOVE, REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
}

private fun readInetPortFile(target: NioPath): Int {
	val bb = ByteBuffer.allocate(2)
	FileChannel.open(target, READ).use {
		it.read(bb)
	}
	// Flip then get, to throw if not enough bytes read.
	return bb.flip().short.toInt() and 0xFFFF
}

@Suppress("NOTHING_TO_INLINE")
private inline fun isLikelyUnixSock(target: NioPath): Boolean = try {
	Files.readAttributes(target, BasicFileAttributes::class.java).isOther
} catch (_: IOException) {
	// The file likely doesn't exist. Let other parts of the codebase discover
	// the actual error. For now, assume that it simply doesn't exist.
	false
}

private fun AutoCloseable.closeInCatch(ex: Throwable) {
	try {
		close()
	} catch (exx: Throwable) {
		ex.addSuppressed(exx)
	}
}
