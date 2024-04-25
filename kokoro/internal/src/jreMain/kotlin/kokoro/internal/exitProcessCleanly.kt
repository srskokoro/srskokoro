package kokoro.internal

import kokoro.internal.CleanProcessExit.doExit as exitProcessCleanly_
import kokoro.internal.CleanProcessExit.doExitLater as exitProcessCleanlyLater_
import kokoro.internal.CleanProcessExit.doExitNonBlocking as exitProcessCleanlyNonBlocking_
import kokoro.internal.CleanProcessExit.status as status_

/**
 * @see CleanProcessExit.doExit
 * @see CleanProcessExit.isDoExitNonBlocking
 */
@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanly(status: Int): Nothing {
	status_ = status
	exitProcessCleanly()
}

/**
 * @see CleanProcessExit.doExit
 * @see CleanProcessExit.isDoExitNonBlocking
 */
@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanly(): Nothing = exitProcessCleanly_()

/**
 * @see CleanProcessExit.doExitLater
 */
@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanlyLater(status: Int) {
	status_ = status
	exitProcessCleanlyLater()
}

/**
 * @see CleanProcessExit.doExitLater
 */
@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanlyLater(): Unit = exitProcessCleanlyLater_()

/**
 * @see CleanProcessExit.doExitNonBlocking
 */
@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanlyNonBlocking(status: Int): Nothing {
	status_ = status
	exitProcessCleanlyNonBlocking()
}

/**
 * @see CleanProcessExit.doExitNonBlocking
 */
@Suppress("NOTHING_TO_INLINE")
inline fun exitProcessCleanlyNonBlocking(): Nothing = exitProcessCleanlyNonBlocking_()
