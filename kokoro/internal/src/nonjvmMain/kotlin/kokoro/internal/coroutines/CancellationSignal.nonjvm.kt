package kokoro.internal.coroutines

import kotlinx.coroutines.CancellationException

actual class CancellationSignal : CancellationException(null)
