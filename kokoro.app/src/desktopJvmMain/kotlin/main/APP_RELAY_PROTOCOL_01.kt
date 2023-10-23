package main

import main.cli.engine.ExecutionState
import okio.BufferedSource
import okio.EOFException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException

internal object APP_RELAY_PROTOCOL_01 {
	const val ID = 1

	fun cast(args: Array<out String>, buffer: okio.Buffer): Long {
		if (ID > Byte.MAX_VALUE) throw AssertionError(
			"Should be implemented as a varint at this point"
		)
		buffer.writeByte(ID)

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

		return size
	}

	fun consume(source: BufferedSource): ExecutionState {
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
					throw AppDaemon.AbortClientConnection()
				}
				else -> throw ex
			}
		}

		// Close the client connection early, as we might be about to run for a
		// very long time. Doing so may throw -- let it!
		source.close()

		return establishClientMain(workingDir, args)
	}
}
