@file:Suppress("ClassName")

package kokoro.internal

import kokoro.internal.scoping.RequiresCompilerPlugin
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

/**
 * Marks a declaration as visible only inside its *scoping package* and any
 * nested packages under that scoping package.
 *
 * By default, the scoping package is the current package of the marked
 * declaration, but this can be changed by providing a *scoping reference* (via
 * the [scopingRef] parameter).
 *
 * @param scopingRef the *scoping reference*; a [KClass] whose package will be
 * used as the scoping package for the marked declaration.
 *
 * @see packagePrivate
 */
@RequiresCompilerPlugin
@RequiresOptIn("Not accessible to packages other than the scoping package and its subpackages.")
@Target(CLASS, PROPERTY, FIELD, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS)
annotation class packageScoped(val scopingRef: KClass<out ScopingRef> = Nothing::class)
