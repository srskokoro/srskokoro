package kokoro.internal.test.support

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.file.FileSystemException
import kotlin.random.Random

private typealias ThrowableFactory = (message: String?) -> Throwable

class ThrowableArb(
	private val circularRefsProb: Double = 0.10,
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
		private const val THROWABLE_MESSAGE_SAMPLES_COUNT = 5

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
	private val messages = ArrayDeque<String?>(initialCapacity = THROWABLE_MESSAGE_EDGECASES.size + THROWABLE_MESSAGE_SAMPLES_COUNT)
	private val stackTraceSizeHints = ArrayDeque<Int>(initialCapacity = STACKTRACE_ARRAY_SIZE_HINTS.size)

	private fun Random.nextSimpleThrowable(): Throwable {
		if (throwableFactories.isEmpty()) {
			throwableFactories.addAll(THROWABLE_FACTORIES)
			throwableFactories.shuffle(this)
		}
		if (messages.isEmpty()) {
			messages.addAll(THROWABLE_MESSAGE_EDGECASES)
			var n = THROWABLE_MESSAGE_SAMPLES_COUNT
			while (--n >= 0) {
				messages.add(nextString(nextIntFavorSmall(100) + 1))
			}
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

	private fun Random.randomizeCauseAndSuppressions(throwable: Throwable, randomizationDepth: Int, throwablesSoFar: ArrayList<Throwable>) {
		if (circularRefsProb > 0.0) throwablesSoFar.add(throwable)

		if (randomizationDepth <= 0) return
		val nextRandomizationDepth = randomizationDepth - 1

		while (nextBoolean()) {
			var sx: Throwable
			kotlin.run {
				if (nextDouble() < circularRefsProb) {
					sx = throwablesSoFar.run { this[nextInt(size)] }
					if (sx !== throwable) return@run
				}
				sx = nextSimpleThrowable()
				randomizeCauseAndSuppressions(sx, nextRandomizationDepth, throwablesSoFar)
			}
			throwable.addSuppressed(sx)
		}
		if (nextBoolean()) {
			var cause: Throwable
			kotlin.run {
				if (nextDouble() < circularRefsProb) {
					cause = throwablesSoFar.run { this[nextInt(size)] }
					if (cause !== throwable) return@run
				}
				cause = nextSimpleThrowable()
				randomizeCauseAndSuppressions(cause, nextRandomizationDepth, throwablesSoFar)
			}
			throwable.initCause(cause)
		}
	}

	private fun Random.nextComplexThrowable(): Throwable {
		val ex = nextSimpleThrowable()
		randomizeCauseAndSuppressions(ex, nextInt(10), ArrayList())
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
		/* classLoaderName = */ if (nextInt(5) != 0) null else nextFqnFragment(),
		/* moduleName = */ if (nextInt(4) != 0) null else nextFqnFragment(),
		/* moduleVersion = */
		if (nextBoolean()) null else buildString {
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
		/* declaringClass = */
		buildString {
			var fragments = nextIntFavorSmall(10)
			while (--fragments >= 0) {
				nextFqnFragment(this)
				append('.')
			}
			nextFqnFragment(this)
		},
		/* methodName = */ nextFqnFragment(),
		/* fileName = */
		if (nextInt(4) != 0) buildString {
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
		/* lineNumber = */ nextIntFavorSmall(1 shl nextInt(15)) - 2,
	)
}
