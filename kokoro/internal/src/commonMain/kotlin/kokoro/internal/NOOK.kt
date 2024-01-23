package kokoro.internal

/**
 * Use with [@RequiresOptIn][RequiresOptIn] (as its [message][RequiresOptIn.message])
 * for creating `@nook` annotations. Example:
 *
 * ```kt
 * @RequiresOptIn(NOOK)
 * annotation class nook
 * ```
 *
 * `@nook` annotations are used as a convention for enforcing package-scoped
 * visibility: declarations annotated with an `@nook` annotation should only
 * really be accessed within the same package as the `@nook` annotation or
 * subpackages of the said package.
 *
 * Also, as a convention, when opting in with an `@nook` annotation and the
 * target is either the entire file or the top-most declaration (such as a
 * class), specify the fully qualified name of the `@nook` annotation. Example:
 *
 * ```
 * @file:OptIn(com.example.nook::class)
 *
 * package com.example.foo.bar
 *
 * // ...
 * ```
 */
const val NOOK = "Not meant to be accessed other than within the same package as the `@nook` annotation or subpackages of that package."
