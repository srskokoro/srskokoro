package kokoro.internal

import kokoro.internal.CleanProcessExit.run as exitProcessCleanlyLater_
import kokoro.internal.CleanProcessExit.runBlocking as exitProcessCleanly_
import kokoro.internal.CleanProcessExit.status as status_

@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanly(status: Int): Nothing {
	status_ = status
	exitProcessCleanly()
}

@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanly(): Nothing = exitProcessCleanly_()

@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanlyLater(status: Int) {
	status_ = status
	exitProcessCleanlyLater()
}

@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanlyLater(): Unit = exitProcessCleanlyLater_()
