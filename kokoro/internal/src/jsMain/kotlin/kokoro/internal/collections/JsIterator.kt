package kokoro.internal.collections

/**
 * Exposes the JavaScript [iterator protocol](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Iteration_protocols#the_iterator_protocol) to Kotlin.
 */
external interface JsIterator<out T> {

	operator fun next(): JsIteratorResult<T>
}

external interface JsIteratorResult<out T> {
	val done: Boolean
	val value: T
}

inline operator fun <T> JsIterator<T>.iterator() = KIterator(this)

class KIterator<out T>(val js: JsIterator<T>) : Iterator<T> {
	private var result = js.next()

	override fun hasNext(): Boolean = !result.done

	override fun next(): T {
		val r = result
		if (!r.done) {
			result = js.next()
			return r.value
		}
		throw NoSuchElementException()
	}
}
