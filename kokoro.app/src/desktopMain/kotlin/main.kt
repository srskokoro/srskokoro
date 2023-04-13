/**
 * An offset to a lock byte that can be locked on in the lock file.
 *
 * This lock byte is what a process should lock on whenever it needs to update
 * the application instance count, add more application instances, designate a
 * particular process as the master instance, etc.
 *
 * @see MASTER_INSTANCE_LOCK_BYTE
 */
private const val INSTANCE_CHANGE_LOCK_BYTE = 0L

/**
 * An offset to a lock byte that can be locked on in the lock file.
 *
 * This lock byte is what a process should lock on in order to designate itself
 * as the master instance of the application's single-program process model.
 *
 * This lock must only be acquired and/or released while locking on [INSTANCE_CHANGE_LOCK_BYTE].
 */
private const val MASTER_INSTANCE_LOCK_BYTE = 1L

fun main(args: Array<out String>) {
}
