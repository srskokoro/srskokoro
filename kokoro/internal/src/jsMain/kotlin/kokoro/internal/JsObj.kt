@file:Suppress("NOTHING_TO_INLINE")

package kokoro.internal

/**
 * A `typealias` to avoid clash with [kotlinx.serialization.json.Json]
 *
 * @see kotlin.js.Json
 */
typealias JsObj = kotlin.js.Json

/**
 * @see JsObj
 */
inline fun JsObj(): JsObj = @Suppress("UnsafeCastFromDynamic") js("{}")

/**
 * Counterpart of [asJsObj] that is not an extension function; useful for the
 * `dynamic` type (since it currently cannot have extension functions).
 */
inline fun JsObj(any: dynamic): JsObj = @Suppress("UnsafeCastFromDynamic") any

/**
 * @see JsObj
 * @see asJson
 */
inline fun Any?.asJsObj(): JsObj = @Suppress("UnsafeCastFromDynamic") asDynamic()

/**
 * @see kotlin.js.Json
 * @see asJsObj
 */
inline fun Any?.asJson(): kotlin.js.Json = @Suppress("UnsafeCastFromDynamic") asDynamic()
