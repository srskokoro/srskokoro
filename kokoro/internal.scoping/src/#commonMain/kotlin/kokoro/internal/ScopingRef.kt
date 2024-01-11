package kokoro.internal

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPEALIAS
import kotlin.reflect.KClass

/**
 * Marks a class or typealias so that it may be used as a *scoping reference*
 * for [packageScoped] and [packagePrivate].
 *
 * This annotation exists to prevent accidental usage of a [KClass] not meant to
 * be used as a scoping reference.
 *
 * @see packageScoped.scopingRef
 * @see packagePrivate.scopingRef
 */
@Target(CLASS, TYPEALIAS)
annotation class ScopingRef
