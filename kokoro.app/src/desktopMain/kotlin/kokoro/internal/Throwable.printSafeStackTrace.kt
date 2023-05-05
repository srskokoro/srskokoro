package kokoro.internal

import kokoro.internal.io.UnsafeCharArrayWriter
import java.io.PrintStream
import java.io.PrintWriter

private typealias OnFailure = (Throwable) -> Unit

@PublishedApi
internal val ON_FAILURE_IGNORE: OnFailure = {}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.getSafeStackTrace() = getSafeStackTrace(ON_FAILURE_IGNORE)

fun Throwable.getSafeStackTrace(onFailure: OnFailure): String {
	return UnsafeCharArrayWriter().run {
		printSafeStackTrace(this, onFailure)
		toString()
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace() = printSafeStackTrace(ON_FAILURE_IGNORE)

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(noinline onFailure: OnFailure) = printSafeStackTrace(System.err, onFailure)

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(out: PrintStream) = printSafeStackTrace(out, ON_FAILURE_IGNORE)

fun Throwable.printSafeStackTrace(out: PrintStream, onFailure: OnFailure) {
	UnsafeCharArrayWriter().run {
		printSafeStackTrace(this, onFailure)
		out.print(toString())
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(out: PrintWriter) = printSafeStackTrace(out, ON_FAILURE_IGNORE)

fun Throwable.printSafeStackTrace(out: PrintWriter, onFailure: OnFailure) {
	UnsafeCharArrayWriter().run {
		printSafeStackTrace(this, onFailure)
		writeTo(out)
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable.printSafeStackTrace(out: UnsafeCharArrayWriter) = printSafeStackTrace(out, ON_FAILURE_IGNORE)

fun Throwable.printSafeStackTrace(out: UnsafeCharArrayWriter, onFailure: OnFailure) {
	val mark = out.size()
	try {
		printStackTrace(PrintWriter(out))
	} catch (ex: Throwable) {
		onFailure(ex)
		try {
			out.truncate(mark) // Reset
			printSafeStackTrace_fallback(out, onFailure)
		} catch (ex: Throwable) {
			onFailure(ex)
		}
	}
}

// --

private const val CAUSE_CAPTION = "Caused by: "
private const val SUPPRESSED_CAPTION = "Suppressed: "

private fun Throwable.safeToString(onFailure: OnFailure) = try {
	toString()
} catch (ex: Throwable) {
	onFailure(ex)
	"[CALL ATTEMPT FAILED: ${javaClass.name}.toString()]"
}

private fun Throwable.safeGetStackTrace(out: UnsafeCharArrayWriter, onFailure: OnFailure): Array<StackTraceElement> = try {
	stackTrace
} catch (ex: Throwable) {
	onFailure(ex)
	out.println("[CALL ATTEMPT FAILED: ${javaClass.name}.getStackTrace()]")
	emptyArray()
}

private fun Throwable.safeGetCause(out: UnsafeCharArrayWriter, onFailure: OnFailure) = try {
	cause
} catch (ex: Throwable) {
	onFailure(ex)
	out.println("[CALL ATTEMPT FAILED: ${javaClass.name}.getCause()]")
	null
}

private fun UnsafeCharArrayWriter.println(value: String) {
	write(value)
	write(System.lineSeparator())
}

internal fun Throwable.printSafeStackTrace_fallback(out: UnsafeCharArrayWriter, onFailure: OnFailure) {
	val dejaVu = ThrowableDejaVuSet()
	dejaVu.add(this)

	out.println(safeToString(onFailure))

	// Print our stack trace
	val trace = safeGetStackTrace(out, onFailure)
	for (traceElement in trace)
		out.println("\tat $traceElement")

	// Print suppressed exceptions, if any
	for (sx in suppressed)
		sx.printEnclosedSafeStackTrace(out, trace, SUPPRESSED_CAPTION, "\t", dejaVu, onFailure)

	// Print cause, if any
	safeGetCause(out, onFailure)
		?.printEnclosedSafeStackTrace(out, trace, CAUSE_CAPTION, "", dejaVu, onFailure)
}

private fun Throwable.printEnclosedSafeStackTrace(
	out: UnsafeCharArrayWriter,
	enclosingTrace: Array<StackTraceElement>,
	caption: String, prefix: String,
	dejaVu: ThrowableDejaVuSet,
	onFailure: OnFailure,
) {
	safeToString(onFailure).let { toString ->
		if (dejaVu.add(this)) {
			// Print our stack trace
			out.println("$prefix$caption$toString")
		} else {
			out.println("$prefix$caption[CIRCULAR REFERENCE: $toString]")
			return // Skip
		}
	}

	// Compute number of frames in common between this and enclosing trace
	val trace = safeGetStackTrace(out, onFailure)
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
		sx.printEnclosedSafeStackTrace(out, trace, SUPPRESSED_CAPTION, prefix + "\t", dejaVu, onFailure)

	// Print cause, if any
	safeGetCause(out, onFailure)
		?.printEnclosedSafeStackTrace(out, trace, CAUSE_CAPTION, prefix, dejaVu, onFailure)
}
