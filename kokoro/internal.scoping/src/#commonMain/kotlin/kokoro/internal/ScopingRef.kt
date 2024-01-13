package kokoro.internal

/**
 * Must be implemented by a class used as a *scoping reference* for
 * [packageScoped] and [packagePrivate]. By convention, the class is an `object`
 * declaration named `module` (lowercase):
 *
 * ```kt
 * @Suppress("ClassName")
 * object module : ScopingRef
 * ```
 *
 * @see packageScoped.scopingRef
 * @see packagePrivate.scopingRef
 */
interface ScopingRef
