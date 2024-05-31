package kokoro.internal.collections

import kokoro.internal.unsafeCastInfer

/**
 * Exposes the JavaScript [iterable protocol](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Iteration_protocols#the_iterable_protocol) to Kotlin.
 *
 * @see jsIterator
 */
external interface JsIterable<out T>

inline fun <T> JsIterable<T>.jsIterator(): JsIterator<T> = unsafeCastInfer(asDynamic()[js("Symbol.iterator")]())

inline operator fun <T> JsIterable<T>.iterator() = jsIterator().iterator()

inline fun <T> JsIterable<T>.asIterable() = KIterable(this)

value class KIterable<out T>(val js: JsIterable<T>) : Iterable<T> {

	@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
	override inline fun iterator() = js.iterator()
}
