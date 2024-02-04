package kokoro.internal

/**
 * Use with [@RequiresOptIn][RequiresOptIn] (as its [message][RequiresOptIn.message])
 * for creating `@nook` annotations. Example:
 *
 * ```kt
 * @NookContract
 * @RequiresOptIn(NOOK)
 * annotation class nook
 * ```
 *
 * `@nook` annotations are used as a convention for enforcing package-scoped
 * visibility: declarations annotated with an `@nook` annotation should only
 * really be accessed within the same package as the `@nook` annotation or
 * subpackages of the said package.
 *
 * @see NookContract
 */
@NookContract
const val NOOK = "Not meant to be accessed other than within the same package as the `@nook` annotation or subpackages of that package."
