import kokoro.app.AppBuild
import kokoro.app.AppData
import kokoro.app.cli.Main
import kokoro.internal.kotlin.TODO
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import okio.BufferedSource
import okio.EOFException
import okio.buffer
import okio.sink
import okio.source
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
	val lockRaf = RandomAccessFile(lockFile, "rw") // May throw; let it!

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

			relay.doForward(args)
		}
	} catch (ex: Throwable) {
		lockChannel.closeInCatch(ex) // Releases all locks
		throw ex
	}
}

// --

private const val CLI_PROTOCOL_01 = 0x01
private const val CLI_PROTOCOL_DEFAULT = CLI_PROTOCOL_01

private object ClientHandlingSwingScope : CoroutineScope {
	override val coroutineContext = SupervisorJob() + Dispatchers.Swing
}

private class AppDaemon(
	sockDir: String,

	private val lockChannel: FileChannel,
	private val masterInstanceLock: FileLock,

	initialArgs: Array<out String>,
) {
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
		ClientHandlingSwingScope.launch {
			handleAppInstance {
				executeMain(System.getProperty("user.dir"), initialArgs)
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
			server.closeInCatch(ex)
			throw ex
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
					client.use {
						handleAppInstance {
							try {
								sendVersionCode(client)
								val source = Channels.newInputStream(client).source().buffer()
								when (val protocol = source.readByte().toInt()) {
									CLI_PROTOCOL_01 -> CLI_PROTOCOL_01_impl(source)
									else -> throw UnsupportedOperationException(
										"Unknown CLI protocol: 0x${protocol.toString(16)} ($protocol)")
								}
							} catch (ex: IOException) {
								when (ex) {
									is ClosedChannelException, is EOFException -> {
										// Client disconnected early.
										// Perhaps its process got killed.
										// Do nothing.
									}
									else -> throw ex
								}
							}
						}
					}
				}
			}
		} catch (ex: ClosedChannelException) {
			// Daemon shutdown was requested.
			// Do nothing.
		} catch (ex: Throwable) {
			server.closeInCatch(ex)
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
			if (ex is ClosedChannelException) throw ex
			throw ClosedChannelException().apply { initCause(ex) }
		}
	}

	@Suppress("FunctionName")
	private suspend fun CLI_PROTOCOL_01_impl(source: BufferedSource) {
		val workingDir: String
		val args: Array<String>

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

		@Suppress("BlockingMethodInNonBlockingContext")
		// Close the client connection early, as we might be about to run for a
		// very long time. Doing so may throw -- let it!
		source.close()

		withContext(Dispatchers.Swing) {
			executeMain(workingDir, args)
		}
	}

	// --

	private suspend inline fun CoroutineScope.executeMain(workingDir: String, args: Array<out String>) {
		Main().feed(workingDir, args, this)
	}

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
		if (0 > observedCount && observedCount > Int.MIN_VALUE) {
			// Daemon already shut down.
			// Pretend that our process got killed while processing a client.
			return // Do nothing. We're exiting anyway.
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

	fun doForward(args: Array<out String>) {
		val version = serverVersionCode
		if (version == AppBuild.VERSION_CODE) {
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
			} catch (_: IOException) {
				// Will close the client connection for us
				showErrorThenExit(versionOrErrorCode = E_SERVICE_HALT)
				return // Skip everything below
			}
			// Deliberately closing outside of any `try` or `use`, as `close()`
			// may throw and interfere with our custom error handling.
			sink.close() // May throw; let it!
			// Done!
		} else showErrorThenExit(
			versionOrErrorCode =
			if (version >= 0) version
			else E_VERSION_BEYOND
		)
	}

	private fun showErrorThenExit(versionOrErrorCode: Int) {
		var thrownByClose: Throwable? = null
		try {
			client.close()
		} catch (ex: Throwable) {
			thrownByClose = ex
		}
		SwingUtilities.invokeLater {
			// TODO Display error dialog for incompatible version or service
			//  halt. Also, interpret 0 as unknown error.
			// TODO Display stacktrace dialog for `thrownByClose` if nonnull
			exitProcess(1)
		}
	}

	companion object {
		const val E_VERSION_BEYOND = -1
		const val E_SERVICE_HALT = -2
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

private fun readInetPortFile(target: NioPath, client: SocketChannel): Int {
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
