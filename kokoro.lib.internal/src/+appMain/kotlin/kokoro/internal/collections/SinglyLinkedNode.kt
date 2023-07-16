package kokoro.internal.collections

import kotlin.jvm.JvmField

abstract class SinglyLinkedNode<Self : SinglyLinkedNode<Self>> {
	@JvmField var next: Self? = null

	class Iterator<T : SinglyLinkedNode<out T>>(@JvmField var next: T?) : kotlin.collections.Iterator<T> {

		@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
		override inline operator fun hasNext(): Boolean = next != null

		@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
		override inline operator fun next(): T {
			val r = next ?: throw NoSuchElementException()
			next = r.next
			return r
		}
	}
}

// NOTE: Deliberately not an `operator`, so that an explicit `asIterable()` (or
// just `iterator()`) would be needed to iterate via the `for`-loop syntax.
@Suppress("NOTHING_TO_INLINE")
inline fun <T : SinglyLinkedNode<out T>> T?.iterator() = SinglyLinkedNode.Iterator(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <T : SinglyLinkedNode<out T>> T?.asIterable() = Iterable { iterator() }

@Suppress("NOTHING_TO_INLINE")
inline fun <T : SinglyLinkedNode<out T>> T?.asSequence() = Sequence { iterator() }

//region Convenience

@Suppress("NOTHING_TO_INLINE")
inline fun <T : SinglyLinkedNode<in T>> T.chain(element: T): T {
	next = element
	return element
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : SinglyLinkedNode<in T>> T.plusAssign(element: T) {
	next = element
}

/**
 * Performs the given [action] on each element.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : SinglyLinkedNode<out T>> T?.forEach(action: (T) -> Unit) {
	var element: T? = this
	while (element != null) {
		action(element)
		element = element.next
	}
}

/**
 * Performs the given [action] on each element, providing sequential index with
 * the element.
 * @param [action] function that takes the index of an element and the element
 * itself and performs the action on the element.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : SinglyLinkedNode<out T>> T?.forEachIndexed(action: (index: Int, T) -> Unit) {
	var index = 0
	var element: T? = this
	while (element != null) {
		action(index++, element)
		element = element.next
	}
}

//endregion
