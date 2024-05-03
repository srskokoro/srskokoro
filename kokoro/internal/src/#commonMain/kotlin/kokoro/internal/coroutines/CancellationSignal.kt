package kokoro.internal.coroutines

import kotlinx.coroutines.CancellationException

expect class CancellationSignal() : CancellationException
