package kokoro.internal

import kokoro.internal.io.UnsafeCharArrayWriter
import java.io.PrintStream
import java.io.PrintWriter

private typealias Failures = ArrayList<Throwable>

@PublishedApi
internal val ON_FAILURES_IGNORE: Throwable.(List<Throwable>) -> Unit = {}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.getSafeStackTrace() = getSafeStackTrace(ON_FAILURES_IGNORE)

fun Throwable.getSafeStackTrace(onFailures: Throwable.(List<Throwable>) -> Unit): String {
	return UnsafeCharArrayWriter().run {
		printSafeStackTrace(this, onFailures)
		toString()
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace() = printSafeStackTrace(ON_FAILURES_IGNORE)

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(noinline onFailures: Throwable.(List<Throwable>) -> Unit) = printSafeStackTrace(System.err, onFailures)

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(out: PrintStream) = printSafeStackTrace(out, ON_FAILURES_IGNORE)

fun Throwable.printSafeStackTrace(out: PrintStream, onFailures: Throwable.(List<Throwable>) -> Unit) {
	UnsafeCharArrayWriter().run {
		printSafeStackTrace(this, onFailures)
		out.print(toString())
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(out: PrintWriter) = printSafeStackTrace(out, ON_FAILURES_IGNORE)

fun Throwable.printSafeStackTrace(out: PrintWriter, onFailures: Throwable.(List<Throwable>) -> Unit) {
	UnsafeCharArrayWriter().run {
		printSafeStackTrace(this, onFailures)
		writeTo(out)
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(out: UnsafeCharArrayWriter) = printSafeStackTrace(out, ON_FAILURES_IGNORE)

fun Throwable.printSafeStackTrace(out: UnsafeCharArrayWriter, onFailures: Throwable.(List<Throwable>) -> Unit) {
	val mark = out.size()
	try {
		printStackTrace(PrintWriter(out))
	} catch (ex: Throwable) {
		val failures = Failures()
		failures.add(ex)
		try {
			out.truncate(mark) // Reset
			printSafeStackTrace_fallback(out, failures)
		} catch (ex: Throwable) {
			failures.add(ex)
		}
		onFailures(failures)
	}
}

// --

private const val CAUSE_CAPTION = "Caused by: "
private const val SUPPRESSED_CAPTION = "Suppressed: "

private fun Throwable.safeToString(failures: Failures) = try {
	toString()
} catch (ex: Throwable) {
	failures.add(ex)
	"[CALL ATTEMPT FAILED: ${javaClass.name}.toString()]"
}

private fun Throwable.safeGetStackTrace(out: UnsafeCharArrayWriter, failures: Failures): Array<StackTraceElement> = try {
	stackTrace
} catch (ex: Throwable) {
	failures.add(ex)
	out.println("[CALL ATTEMPT FAILED: ${javaClass.name}.getStackTrace()]")
	emptyArray()
}

private fun Throwable.safeGetCause(out: UnsafeCharArrayWriter, failures: Failures) = try {
	cause
} catch (ex: Throwable) {
	failures.add(ex)
	out.println("[CALL ATTEMPT FAILED: ${javaClass.name}.getCause()]")
	null
}

private fun UnsafeCharArrayWriter.println(value: String) {
	write(value)
	write(System.lineSeparator())
}

internal fun Throwable.printSafeStackTrace_fallback(out: UnsafeCharArrayWriter, failures: Failures) {
	val dejaVu = ThrowableDejaVuSet()
	dejaVu.add(this)

	out.println(safeToString(failures))

	// Print our stack trace
	val trace = safeGetStackTrace(out, failures)
	for (traceElement in trace)
		out.println("\tat $traceElement")

	// Print suppressed exceptions, if any
	for (sx in suppressed)
		sx.printEnclosedSafeStackTrace(out, trace, SUPPRESSED_CAPTION, "\t", dejaVu, failures)

	// Print cause, if any
	safeGetCause(out, failures)
		?.printEnclosedSafeStackTrace(out, trace, CAUSE_CAPTION, "", dejaVu, failures)
}

private fun Throwable.printEnclosedSafeStackTrace(
	out: UnsafeCharArrayWriter,
	enclosingTrace: Array<StackTraceElement>,
	caption: String, prefix: String,
	dejaVu: ThrowableDejaVuSet,
	failures: Failures,
) {
	safeToString(failures).let { toString ->
		if (dejaVu.add(this)) {
			// Print our stack trace
			out.println("$prefix$caption$toString")
		} else {
			out.println("$prefix$caption[CIRCULAR REFERENCE: $toString]")
			return // Skip
		}
	}

	// Compute number of frames in common between this and enclosing trace
	val trace = safeGetStackTrace(out, failures)
	var m = trace.size - 1
	var n = enclosingTrace.size - 1
	while (m >= 0 && n >= 0 && trace[m] == enclosingTrace[n]) {
		m--; n--
	}
	val framesInCommon = trace.size - 1 - m

	// Print our stack trace
	for (i in 0..m)
		out.println("$prefix\tat ${trace[i]}")
	if (framesInCommon != 0)
		out.println("$prefix\t... $framesInCommon more")

	// Print suppressed exceptions, if any
	for (sx in suppressed)
		sx.printEnclosedSafeStackTrace(out, trace, SUPPRESSED_CAPTION, prefix + "\t", dejaVu, failures)

	// Print cause, if any
	safeGetCause(out, failures)
		?.printEnclosedSafeStackTrace(out, trace, CAUSE_CAPTION, prefix, dejaVu, failures)
}
