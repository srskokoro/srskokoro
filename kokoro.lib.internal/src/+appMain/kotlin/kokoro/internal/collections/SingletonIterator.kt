package kokoro.internal.collections

class SingletonIterator<out T : Any>(element: T) : Iterator<T> {
	private var element: T? = element

	override fun next(): T {
		val e = element
		if (e != null) {
			element = null
			return e
		}
		throw NoSuchElementException()
	}

	override fun hasNext() = element != null
}
