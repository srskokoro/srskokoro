﻿package kokoro.internal.test.support

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import kokoro.internal.StackTraceElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.file.FileSystemException
import kotlin.math.max
import kotlin.random.Random

private typealias ThrowableFactory = (message: String?) -> Throwable

class ThrowableArb(
	private val circularRefsProb: Double = 0.10,
	private val maxThrowableCount: Int = 20,
) : Arb<Throwable>() {
	companion object {
		private val THROWABLE_FACTORIES = listOf<ThrowableFactory>(
			{ Throwable(it) },
			{ object : Throwable(it) {} },

			{ Error(it) },
			{ Exception(it) },
			{ RuntimeException(it) },

			{ if (it == null) NotImplementedError() else NotImplementedError(it) },
			{ AssertionError(it) },
			{ VerifyError(it) },

			{ IOException(it) },
			{ InterruptedIOException(it) },
			{ FileSystemException(it) },

			{ IllegalStateException(it) },
			{ IllegalArgumentException(it) },
			{ InterruptedException(it) },
		)

		private val THROWABLE_MESSAGE_EDGECASES = listOf(null, "", "a", "\u0000")
		private const val THROWABLE_MESSAGE_OF_RANDOM_ASCII_COUNT = 2
		private const val THROWABLE_MESSAGE_OF_RANDOM_PRINTABLE_ASCII_COUNT = 3

		private val EMPTY_STACKTRACE = emptyArray<StackTraceElement>()

		// -2 -> reuse a preset stacktrace
		// -1 -> preserve current stacktrace
		// 0 -> empty stacktrace
		// 1 -> randomized stacktrace of size 1
		// 2 -> randomized stacktrace of size 2
		// 3 -> randomized stacktrace of size 3 or more
		private val STACKTRACE_ARRAY_SIZE_HINTS = listOf(-2, -2, -1, -1, 0, 1, 2, 3, 3, 3)

		private object PRESET_STACKTRACE_SAMPLE {
			val value: Array<StackTraceElement> = runBlocking(Dispatchers.IO) {
				sequence {
					yield(Throwable())
				}.iterator().next()
			}.stackTrace
		}

		private val FQN_FRAGMENT_CHARS = ('a'..'z') + ('A'..'Z') + DIGIT_CHARS + '$'
	}

	private val throwableFactories = ArrayDeque<ThrowableFactory>(initialCapacity = THROWABLE_FACTORIES.size)
	private val messages = ArrayDeque<String?>(initialCapacity = THROWABLE_MESSAGE_EDGECASES.size + THROWABLE_MESSAGE_OF_RANDOM_PRINTABLE_ASCII_COUNT)
	private val stackTraceSizeHints = ArrayDeque<Int>(initialCapacity = STACKTRACE_ARRAY_SIZE_HINTS.size)

	private fun Random.nextSimpleThrowable(): Throwable {
		if (throwableFactories.isEmpty()) {
			throwableFactories.addAll(THROWABLE_FACTORIES)
			throwableFactories.shuffle(this)
		}
		if (messages.isEmpty()) {
			messages.addAll(THROWABLE_MESSAGE_EDGECASES)
			var n = THROWABLE_MESSAGE_OF_RANDOM_PRINTABLE_ASCII_COUNT + THROWABLE_MESSAGE_OF_RANDOM_ASCII_COUNT
			while (--n >= 0) messages.add(nextString(
				if (n >= THROWABLE_MESSAGE_OF_RANDOM_PRINTABLE_ASCII_COUNT) ASCII_CHARS
				else PRINTABLE_ASCII_CHARS,
				nextIntFavorSmall(100) + 1,
			))
			messages.shuffle(this)
		}
		if (stackTraceSizeHints.isEmpty()) {
			stackTraceSizeHints.addAll(STACKTRACE_ARRAY_SIZE_HINTS)
			stackTraceSizeHints.shuffle(this)
		}

		val throwableFactory = throwableFactories.removeFirst()
		val message = messages.removeFirst()
		val throwable = throwableFactory(message)

		when (val stackTraceSizeHint = stackTraceSizeHints.removeFirst()) {
			-2 -> throwable.stackTrace = PRESET_STACKTRACE_SAMPLE.value
			-1 -> {}
			0 -> throwable.stackTrace = EMPTY_STACKTRACE
			else -> {
				throwable.stackTrace = if (stackTraceSizeHint < 3) {
					Array(stackTraceSizeHint) { nextStackTraceElement() }
				} else {
					Array(nextIntFavorSmall(until = nextInt(1, 100)) + 3) { nextStackTraceElement() }
				}
			}
		}
		return throwable
	}

	private fun Random.randomizeCauseAndSuppressions(current: Throwable, maxThrowableCount: Int, throwablesSoFar: ArrayList<Throwable>) {
		throwablesSoFar.add(current)

		while (throwablesSoFar.size < maxThrowableCount && nextBoolean()) {
			var sx: Throwable
			kotlin.run {
				if (nextDouble() < circularRefsProb) {
					sx = throwablesSoFar.run { this[nextInt(size)] }
					// NOTE: Suppressed cannot be self: it's disallowed.
					if (sx !== current) return@run // Suppression allowed.
				}
				sx = nextSimpleThrowable()
				randomizeCauseAndSuppressions(sx, maxThrowableCount, throwablesSoFar)
			}
			current.addSuppressed(sx)
		}
		if (throwablesSoFar.size < maxThrowableCount && nextBoolean()) {
			var cause: Throwable
			kotlin.run {
				if (nextDouble() < circularRefsProb) {
					cause = throwablesSoFar.run { this[nextInt(size)] }
					// NOTE: Cause cannot be self: it's disallowed.
					if (cause !== current) return@run // Causation allowed.
				}
				cause = nextSimpleThrowable()
				randomizeCauseAndSuppressions(cause, maxThrowableCount, throwablesSoFar)
			}
			current.initCause(cause)
		}
	}

	private fun Random.nextComplexThrowable(): Throwable {
		val ex = nextSimpleThrowable()
		randomizeCauseAndSuppressions(ex, nextInt(max(maxThrowableCount, 0) + 1), ArrayList())
		return ex
	}

	override fun sample(rs: RandomSource): Sample<Throwable> {
		return Sample(rs.random.nextComplexThrowable())
	}

	override fun edgecase(rs: RandomSource): Throwable? = null

	// --

	private fun Random.nextFqnFragment(out: StringBuilder) =
		nextString(out, FQN_FRAGMENT_CHARS, nextIntFavorSmall(32) + 1)

	private fun Random.nextFqnFragment() = buildString { nextFqnFragment(this) }

	private fun Random.nextStackTraceElement() = StackTraceElement(
		classLoaderName = if (nextInt(5) != 0) null else nextFqnFragment(),
		moduleName = if (nextInt(4) != 0) null else nextFqnFragment(),
		moduleVersion = if (nextBoolean()) null else buildString {
			fun nextVersionLength() = nextIntFavorSmallGreatly(3) + 1
			nextString(this, DIGIT_CHARS, nextVersionLength())
			if (nextInt(3) != 0) {
				append('.')
				nextString(this, DIGIT_CHARS, nextVersionLength())
				if (nextInt(4) == 0) {
					append('.')
					nextString(this, DIGIT_CHARS, nextVersionLength())
				}
			}
		},
		declaringClass = buildString {
			var fragments = nextIntFavorSmall(10)
			while (--fragments >= 0) {
				nextFqnFragment(this)
				append('.')
			}
			nextFqnFragment(this)
		},
		methodName = nextFqnFragment(),
		fileName = if (nextInt(4) != 0) buildString {
			nextFqnFragment(this)
			when (nextIntFavorSmall(5)) {
				0 -> append(".kt")
				1 -> append(".java")
				2 -> {}
				3 -> {
					append('.')
					nextFqnFragment(this)
				}
				else -> append('.')
			}
		} else null,
		lineNumber = nextIntFavorSmall(1 shl nextInt(15)) - 2,
	)
}