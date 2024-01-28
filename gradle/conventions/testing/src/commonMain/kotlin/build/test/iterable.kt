package build.test

import assertk.Assert
import assertk.assertions.containsAtLeast
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.containsNone
import assertk.assertions.containsOnly
import assertk.assertions.containsSubList

/**
 * Asserts that this iterable does not contain any of the expected elements.
 *
 * @see containsNone
 * @see hasAtLeast
 */
fun <T> Assert<Iterable<T>>.hasNone(expected: Iterable<T>) {
	given { actual ->
		val actualSet = actual.asSet()
		if (expected.none { it in actualSet }) {
			return
		}
	}
	// NOTE: Depending on how our arguments are implemented, the following may
	// actually not fail. But that's okay.
	containsNone(*expected.asArray())
}

/**
 * Asserts that this iterable contains at least the expected elements, in any
 * order. This iterable may also contain additional elements.
 *
 * @see containsAtLeast
 * @see hasNone
 * @see hasSubList
 * @see hasExactly
 */
fun <T> Assert<Iterable<T>>.hasAtLeast(expected: Iterable<T>) {
	given { actual ->
		val actualSet = actual.asSet()
		if (expected.all { it in actualSet }) {
			return
		}
	}
	// NOTE: Depending on how our arguments are implemented, the following may
	// actually not fail. But that's okay.
	containsAtLeast(*expected.asArray())
}

/**
 * Asserts that this iterable contains exactly the expected elements. They must
 * be in the same order and there must not be any extra elements.
 *
 * @see containsExactly
 * @see hasExactlyInAnyOrder
 * @see hasOnly
 * @see hasAtLeast
 * @see hasSubList
 */
fun <T> Assert<Iterable<T>>.hasExactly(expected: Iterable<T>) {
	given { actual ->
		val actual_iter = actual.iterator()
		val expected_iter = expected.iterator()
		while (true) {
			if (expected_iter.hasNext()) {
				if (!actual_iter.hasNext()) return@given
				if (actual_iter.next() != expected_iter.next()) return@given
			} else {
				if (actual_iter.hasNext()) return@given
				return // Done. Skip code below.
			}
		}
	}
	// NOTE: Depending on how our arguments are implemented, the following may
	// actually not fail. But that's okay.
	transform { it.asList() }.containsExactly(*expected.asArray())
}

/**
 * Asserts that this iterable contains exactly the expected elements, in any
 * order. Unlike [hasOnly], each expected value must correspond exactly to a
 * matching value in the actual iterable, and visa-versa.
 *
 * @see containsExactlyInAnyOrder
 * @see hasExactly
 * @see hasOnly
 * @see hasAtLeast
 */
fun <T> Assert<Iterable<T>>.hasExactlyInAnyOrder(expected: Iterable<T>) {
	given { actual ->
		if (actual is Set) {
			var expected_n = 0
			expected.forEach {
				if (it !in actual) return@given
				if (++expected_n < 0) throw E_UnsupportedOnCountOverflow()
			}
			if (actual.size != expected_n) return@given
		} else {
			val counts = HashMap<T, Counter>()
			actual.forEach {
				if (++counts.getOrPut(it) { Counter(0) }.count < 0)
					throw E_UnsupportedOnCountOverflow()
			}
			expected.forEach {
				if (--(counts[it] ?: return@given).count <= 0)
					counts.remove(it)
			}
			if (counts.isNotEmpty()) return@given
		}
		return // Done. Skip code below.
	}
	// NOTE: Depending on how our arguments are implemented, the following may
	// actually not fail. But that's okay.
	containsExactlyInAnyOrder(*expected.asArray())
}

/**
 * Asserts that this iterable contains only the expected elements, in any order.
 * Duplicate values in the expected and actual are ignored.
 *
 * @see containsOnly
 * @see hasExactlyInAnyOrder
 * @see hasExactly
 * @see hasAtLeast
 */
fun <T> Assert<Iterable<T>>.hasOnly(expected: Iterable<T>) {
	given { actual ->
		val actualSet = actual.asSet()
		var expected_n = 0
		expected.forEach {
			if (it !in actualSet) return@given
			if (++expected_n < 0) throw E_UnsupportedOnCountOverflow()
		}
		val actualSet_n = actualSet.size
		if (actualSet_n == expected_n || actualSet_n == expected.asSet().size) {
			return // Done. Skip code below.
		}
	}
	// NOTE: Depending on how our arguments are implemented, the following may
	// actually not fail. But that's okay.
	containsOnly(*expected.asArray())
}

/**
 * Asserts that this iterable contains at least the expected elements, in the
 * same exact order. This iterable may also contain additional elements.
 *
 * @see hasExactly
 * @see hasAtLeast
 */
fun <T> Assert<Iterable<T>>.hasSubList(expected: Iterable<T>) {
	// Load all the expected values in advance, since we'll have to visit each
	// value more than once in case of a partial match.
	val expectedList = expected.toMutableList()

	given { actual ->
		val actual_iter = actual.iterator()
		if (!actual_iter.hasNext()) {
			if (expectedList.isNotEmpty()) return@given // Fail
			return // Done. Skip code below.
		}

		var a = actual_iter.next()
		reset@ while (true) {
			val expected_iter = expectedList.iterator() // Reset!
			if (!expected_iter.hasNext()) break@reset // Success

			// Find the first match
			val expected_first = expected_iter.next()
			while (a != expected_first) {
				if (!actual_iter.hasNext()) return@given // Fail
				a = actual_iter.next()
			}

			// Try to match the rest
			while (true) {
				if (!expected_iter.hasNext()) break@reset // Success
				if (!actual_iter.hasNext()) return@given // Fail
				a = actual_iter.next()
				if (a != expected_iter.next()) continue@reset // on partial match.
			}
		}
		return // Done. Skip code below.
	}
	// NOTE: Depending on how our arguments are implemented, the following may
	// actually not fail. But that's okay.
	transform { it.asList() }.containsSubList(expectedList)
}

// --

private fun Iterable<*>.asSet(): Set<*> = if (this is Set) this else toHashSet()

private fun Iterable<*>.asList(): List<*> = if (this is List) this else toMutableList()

@Suppress("NOTHING_TO_INLINE")
private inline fun Iterable<*>.asArray(): Array<*> = asList().toTypedArray()

private class Counter(var count: Int)

private fun E_UnsupportedOnCountOverflow() = UnsupportedOperationException("Count overflow")
