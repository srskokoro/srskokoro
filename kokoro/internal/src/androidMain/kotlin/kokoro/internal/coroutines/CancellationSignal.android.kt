package kokoro.internal.coroutines

import kokoro.internal.DEBUG
import kotlinx.coroutines.CancellationException

actual class CancellationSignal : CancellationException() {

	override fun fillInStackTrace(): Throwable {
		if (DEBUG) {
			return super.fillInStackTrace()
		}
		// Prevent Android <= 6.0 bug -- https://github.com/Kotlin/kotlinx.coroutines/issues/1866
		stackTrace = EMPTY_STACKTRACE
		return this
	}

	companion object {
		private val EMPTY_STACKTRACE = emptyArray<StackTraceElement>()
	}
}
