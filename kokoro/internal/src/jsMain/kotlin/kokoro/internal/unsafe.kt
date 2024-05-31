@file:Suppress("NOTHING_TO_INLINE")

package kokoro.internal

inline fun <T> unsafeNull(): T = @Suppress("UnsafeCastFromDynamic") null.asDynamic()

/**
 * Counterpart of [kotlin.js.unsafeCast] that is not an extension function.
 */
inline fun <T> unsafeCast(any: dynamic) = any.unsafeCast<T>()

/**
 * Similar to [kotlin.js.unsafeCast] while allowing type inference.
 */
inline fun <T> Any?.unsafeCastInfer(): T = @Suppress("UnsafeCastFromDynamic") asDynamic()

/**
 * Counterpart of [unsafeCastInfer] that is not an extension function.
 */
inline fun <T> unsafeCastInfer(any: dynamic): T = @Suppress("UnsafeCastFromDynamic") any

// --

/**
 * @see asJsonArray
 * @see asJsObj
 */
inline fun Any?.asJsArray(): Array<dynamic> = @Suppress("UnsafeCastFromDynamic") asDynamic()

/**
 * @see asJsArray
 * @see asJson
 */
inline fun Any?.asJsonArray(): Array<dynamic> = @Suppress("UnsafeCastFromDynamic") asDynamic()
