package main

import kokoro.app.AppBuild
import kokoro.app.ui.ExitProcessNonZeroViaSwing
import kokoro.app.ui.StackTraceModal
import kokoro.app.ui.ifChoiceMatches
import kokoro.app.ui.swingAlert
import kokoro.internal.assertUnreachable
import kokoro.internal.closeInCatch
import okio.sink
import java.awt.EventQueue
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.READ
import java.nio.file.attribute.BasicFileAttributes

internal class AppRelay(sockDir: String) {
	private val client: SocketChannel
	private val serverVersionCode: Int

	init {
		val client: SocketChannel
		val connectAddress: SocketAddress

		val sockPath = Path.of(sockDir, ".sock")
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
			val size = APP_RELAY_PROTOCOL_01.cast(args, buffer)
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
		} catch (ex: ExitMain) {
			// If we're here, then we can safely assume that `client` was
			// already closed for us. Simply rethrow the exception.
			throw ex
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
				swingAlert({
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
				}).ifChoiceMatches({ CustomAction }) {
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
}

/**
 * Reads a file containing the INET port number as a 2-byte sequence in
 * big-endian byte order.
 *
 * @see generateInetPortFile
 */
private fun readInetPortFile(target: Path): Int {
	val bb = ByteBuffer.allocate(2)
	FileChannel.open(target, READ).use {
		it.read(bb)
	}
	// Flip then get, to throw if not enough bytes read.
	return bb.flip().short.toInt() and 0xFFFF
}

@Suppress("NOTHING_TO_INLINE")
private inline fun isLikelyUnixSock(target: Path): Boolean = try {
	Files.readAttributes(target, BasicFileAttributes::class.java).isOther
} catch (_: IOException) {
	// The file likely doesn't exist. Let other parts of the codebase discover
	// the actual error. For now, assume that it simply doesn't exist.
	false
}
