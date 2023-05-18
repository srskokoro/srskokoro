package kokoro.internal.collections

class NullableSingletonIterator<out T : Any?>(private val element: T) : Iterator<T> {
	private var hasNext = true

	override fun next(): T {
		if (hasNext) {
			hasNext = false
			return element
		}
		throw NoSuchElementException()
	}

	override fun hasNext() = hasNext
}
