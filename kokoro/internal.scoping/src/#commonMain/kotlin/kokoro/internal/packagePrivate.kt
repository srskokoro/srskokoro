@file:Suppress("DEPRECATION_ERROR", "ClassName")

package kokoro.internal

import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

/**
 * Marks a declaration as visible only inside its *scoping package*. Unlike
 * [packageScoped], the marked symbol won't be visible to any nested packages of
 * the scoping package.
 *
 * By default, the scoping package is the current package of the marked
 * declaration, but this can be changed by providing a *scoping reference* (via
 * the [scopingRef] parameter).
 *
 * @param scopingRef the *scoping reference*; a [KClass] whose package will be
 * used as the scoping package for the marked declaration.
 *
 * @see packageScoped
 */
@RequiresCompilerPlugin
@RequiresOptIn("Not accessible to packages other than the scoping package.")
@Target(CLASS, PROPERTY, FIELD, LOCAL_VARIABLE, VALUE_PARAMETER, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS)
annotation class packagePrivate(val scopingRef: KClass<out ScopingRef> = Nothing::class)
