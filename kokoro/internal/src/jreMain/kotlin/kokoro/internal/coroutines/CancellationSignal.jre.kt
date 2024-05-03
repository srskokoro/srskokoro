package kokoro.internal.coroutines

import kokoro.internal.DEBUG
import kotlinx.coroutines.CancellationException

actual class CancellationSignal : CancellationException() {

	override fun fillInStackTrace(): Throwable {
		if (DEBUG) {
			return super.fillInStackTrace()
		}
		return this
	}
}
