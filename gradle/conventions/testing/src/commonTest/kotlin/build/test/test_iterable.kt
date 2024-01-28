package build.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isFailure
import io.kotest.core.spec.style.FreeSpec

class test_iterable : FreeSpec({
	// References:
	// - https://github.com/willowtreeapps/assertk/blob/v0.28.0/assertk/src/commonTest/kotlin/test/assertk/assertions/IterableTest.kt
	// - https://github.com/willowtreeapps/assertk/blob/v0.28.0/assertk/src/commonTest/kotlin/test/assertk/assertions/ListTest.kt
	// --

	//#region hasNone
	"hasNone: passes on missing element" {
		assertAll {
			assertThat(iterOf<Int>()).hasNone(iterOf(1).once())
			assertThat(iterOf(2, 3)).hasNone(iterOf(1).once())
			assertThat(iterOf(2, 3)).hasNone(iterOf<Int>().once())
		}
	}
	"hasNone: fails on present element" {
		assertResult {
			assertThat(iterOf(1, 2)).hasNone(iterOf(2, 3))
		}.isFailure().hasMessage("""
			|expected to contain none of:<[2, 3]> but was:<[1, 2]>
			| elements not expected:<[2]>
		""".trimMargin())
	}
	//#endregion

	//#region hasAtLeast
	"hasAtLeast: passes when all elements present" {
		assertThat(iterOf(1, 2)).hasAtLeast(iterOf(2, 1).once())
	}
	"hasAtLeast: fails when some elements missing" {
		assertResult {
			assertThat(iterOf(1)).hasAtLeast(iterOf(1, 2))
		}.isFailure().hasMessage("""
			|expected to contain all:<[1, 2]> but was:<[1]>
			| elements not found:<[2]>
		""".trimMargin())
	}
	//#endregion

	//#region hasExactly
	"hasExactly: passes when all elements present in same order" {
		assertThat(iterOf(1, 2)).hasExactly(iterOf(1, 2).once())
	}
	"hasExactly: fails when all elements present in different order" {
		assertResult {
			assertThat(iterOf(1, 2)).hasExactly(iterOf(2, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<[2, 1]> but was:<[1, 2]>
			| at index:0 expected:<2>
			| at index:1 unexpected:<2>
		""".trimMargin())
	}
	"hasExactly: fails when all elements present in different order (with 3 elements)" {
		assertResult {
			assertThat(iterOf("1", "2", "3")).hasExactly(iterOf("2", "3", "1"))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<["2", "3", "1"]> but was:<["1", "2", "3"]>
			| at index:0 unexpected:<"1">
			| at index:2 expected:<"1">
		""".trimMargin())
	}
	"hasExactly: fails on same length but different elements" {
		assertResult {
			assertThat(iterOf(1, 1)).hasExactly(iterOf(2, 2))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<[2, 2]> but was:<[1, 1]>
			| at index:0 expected:<2>
			| at index:0 unexpected:<1>
			| at index:1 expected:<2>
			| at index:1 unexpected:<1>
		""".trimMargin())
	}
	"hasExactly: fails on shorter length and different elements" {
		assertResult {
			assertThat(iterOf(1, 2)).hasExactly(iterOf(3))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<[3]> but was:<[1, 2]>
			| at index:0 expected:<3>
			| at index:0 unexpected:<1>
			| at index:1 unexpected:<2>
		""".trimMargin())
	}
	"hasExactly: fails on longer length and different elements" {
		assertResult {
			assertThat(iterOf(1)).hasExactly(iterOf(2, 3))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<[2, 3]> but was:<[1]>
			| at index:0 expected:<2>
			| at index:0 unexpected:<1>
			| at index:1 expected:<3>
		""".trimMargin())
	}
	"hasExactly: fails on extra element at the end" {
		assertResult {
			assertThat(iterOf(1, 2)).hasExactly(iterOf(1, 2, 3))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<[1, 2, 3]> but was:<[1, 2]>
			| at index:2 expected:<3>
		""".trimMargin())
	}
	"hasExactly: fails on extra element in the middle" {
		assertResult {
			assertThat(iterOf(1, 3)).hasExactly(iterOf(1, 2, 3))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<[1, 2, 3]> but was:<[1, 3]>
			| at index:1 expected:<2>
		""".trimMargin())
	}
	"hasExactly: fails on missing element in the middle" {
		assertResult {
			assertThat(iterOf(1, 2, 3)).hasExactly(iterOf(1, 3))
		}.isFailure().hasMessage("""
			|expected to contain exactly:<[1, 3]> but was:<[1, 2, 3]>
			| at index:1 unexpected:<2>
		""".trimMargin())
	}
	"hasExactly: passes on custom iterable types with same elements in same order" {
		assertThat(CustomList("one", "two")).hasExactly(CustomList("one", "two"))
		assertThat(CustomList("one", "two").once()).hasExactly(CustomList("one", "two").once())
	}
	"hasExactly: passes on differing iterable types but same elements in same order" {
		assertAll {
			assertThat(CustomList("one", "two")).hasExactly(iterOf("one", "two"))
			assertThat(CustomList("one", "two")).hasExactly(iterOf("one", "two").once())

			assertThat(iterOf("one", "two")).hasExactly(CustomList("one", "two"))
			assertThat(iterOf("one", "two").once()).hasExactly(CustomList("one", "two"))
		}
	}
	//#endregion

	//#region hasExactlyInAnyOrder
	"hasExactlyInAnyOrder: passes when all elements present" {
		assertAll {
			assertThat(iterOf(1, 2)).hasExactlyInAnyOrder(iterOf(2, 1).once())
			assertThat(setOf(1, 2, 1)).hasExactlyInAnyOrder(iterOf(2, 1).once())
		}
	}
	"hasExactlyInAnyOrder: passes when all elements present and duplicates have correspondence" {
		assertThat(iterOf(1, 2, 1)).hasExactlyInAnyOrder(iterOf(2, 1, 1).once())
	}
	"hasExactlyInAnyOrder: fails when all elements present but duplicates have no correspondence" {
		assertResult {
			assertThat(iterOf(1, 2, 2)).hasExactlyInAnyOrder(iterOf(2, 1, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 1, 1]> but was:<[1, 2, 2]>
			| elements not found:<[1]>
			| extra elements found:<[2]>
		""".trimMargin())

		assertResult {
			assertThat(setOf(1, 2, 1)).hasExactlyInAnyOrder(iterOf(2, 1, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 1, 1]> but was:<[1, 2]>
			| elements not found:<[1]>
		""".trimMargin())
	}
	"hasExactlyInAnyOrder: fails when all elements present but duplicates have no correspondence and length is shorter" {
		assertResult {
			assertThat(iterOf(1, 2, 2)).hasExactlyInAnyOrder(iterOf(2, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 1]> but was:<[1, 2, 2]>
			| extra elements found:<[2]>
		""".trimMargin())
	}
	"hasExactlyInAnyOrder: fails when all elements present but duplicates have no correspondence and length is longer" {
		assertResult {
			assertThat(iterOf(1, 2)).hasExactlyInAnyOrder(iterOf(2, 2, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 2, 1]> but was:<[1, 2]>
			| elements not found:<[2]>
		""".trimMargin())

		assertResult {
			assertThat(setOf(1, 2, 1)).hasExactlyInAnyOrder(iterOf(2, 2, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 2, 1]> but was:<[1, 2]>
			| elements not found:<[2]>
		""".trimMargin())
	}
	"hasExactlyInAnyOrder: fails on more elements" {
		assertResult {
			assertThat(iterOf(1, 2, 3)).hasExactlyInAnyOrder(iterOf(2, 1, 3, 4))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 1, 3, 4]> but was:<[1, 2, 3]>
			| elements not found:<[4]>
		""".trimMargin())

		assertResult {
			assertThat(setOf(1, 2, 3, 2)).hasExactlyInAnyOrder(iterOf(2, 1, 3, 4))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 1, 3, 4]> but was:<[1, 2, 3]>
			| elements not found:<[4]>
		""".trimMargin())
	}
	"hasExactlyInAnyOrder: fails on less elements" {
		assertResult {
			assertThat(iterOf(1, 2, 3)).hasExactlyInAnyOrder(iterOf(2, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 1]> but was:<[1, 2, 3]>
			| extra elements found:<[3]>
		""".trimMargin())

		assertResult {
			assertThat(setOf(1, 2, 3, 3)).hasExactlyInAnyOrder(iterOf(2, 1))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2, 1]> but was:<[1, 2, 3]>
			| extra elements found:<[3]>
		""".trimMargin())
	}
	"hasExactlyInAnyOrder: fails on same length but different elements" {
		assertResult {
			assertThat(iterOf(1)).hasExactlyInAnyOrder(iterOf(2))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2]> but was:<[1]>
			| elements not found:<[2]>
			| extra elements found:<[1]>
		""".trimMargin())

		assertResult {
			assertThat(setOf(1, 1)).hasExactlyInAnyOrder(iterOf(2))
		}.isFailure().hasMessage("""
			|expected to contain exactly in any order:<[2]> but was:<[1]>
			| elements not found:<[2]>
			| extra elements found:<[1]>
		""".trimMargin())
	}
	//#endregion

	//#region hasOnly
	"hasOnly: passes when all elements present" {
		assertThat(iterOf(1, 2).once()).hasOnly(iterOf(2, 1).once())
	}
	"hasOnly: passes when all elements present, even with duplicates" {
		assertAll {
			assertThat(iterOf(1, 2, 1).once()).hasOnly(iterOf(2, 1, 1))
			assertThat(iterOf(1, 2, 1).once()).hasOnly(iterOf(2, 1, 2))
			assertThat(iterOf(1, 2, 2).once()).hasOnly(iterOf(2, 1, 1))

			assertThat(iterOf(1, 2, 2).once()).hasOnly(iterOf(2, 1))
			assertThat(iterOf(1, 2).once()).hasOnly(iterOf(2, 1, 1))
		}
	}
	"hasOnly: fails on more elements" {
		assertResult {
			assertThat(iterOf(1, 2, 3)).hasOnly(iterOf(2, 1, 3, 4))
		}.isFailure().hasMessage("""
			|expected to contain only:<[2, 1, 3, 4]> but was:<[1, 2, 3]>
			| elements not found:<[4]>
		""".trimMargin())
	}
	"hasOnly: fails on less elements" {
		assertResult {
			assertThat(iterOf(1, 2, 3)).hasOnly(iterOf(2, 1))
		}.isFailure().hasMessage("""
			|expected to contain only:<[2, 1]> but was:<[1, 2, 3]>
			| extra elements found:<[3]>
		""".trimMargin())
	}
	"hasOnly: fails on same length but different elements" {
		assertResult {
			assertThat(iterOf(1)).hasOnly(iterOf(2))
		}.isFailure().hasMessage("""
			|expected to contain only:<[2]> but was:<[1]>
			| elements not found:<[2]>
			| extra elements found:<[1]>
		""".trimMargin())
	}
	//#endregion

	//#region hasSubList
	"hasSubList: passes on empty sublist" {
		assertAll {
			assertThat(iterOf<String>().once()).hasSubList(iterOf())
			assertThat(iterOf("alice", "bob", "carol").once()).hasSubList(iterOf())
		}
	}
	"hasSubList: passes on exact order" {
		assertAll {
			val actual = iterOf(0, 1, 2, 3, 4, 5, 6)
			assertThat(actual.once()).hasSubList(iterOf(0, 1, 2, 3, 4, 5, 6))
			assertThat(actual.once()).hasSubList(iterOf(0))
			assertThat(actual.once()).hasSubList(iterOf(0, 1))
			assertThat(actual.once()).hasSubList(iterOf(2))
			assertThat(actual.once()).hasSubList(iterOf(2, 3, 4))
			assertThat(actual.once()).hasSubList(iterOf(3, 4))
			assertThat(actual.once()).hasSubList(iterOf(5, 6))
			assertThat(actual.once()).hasSubList(iterOf(6))
		}
	}
	"hasSubList: passes after a failed partial match" {
		val extra = iterOf(0, 1)
		val partialMatch = iterOf(2, 3)

		val expected = partialMatch + iterOf(4, 5)
		val actual = iterOf(extra, partialMatch, extra, expected, iterOf(6)).flatten()

		assertThat(actual.once()).hasSubList(expected)
	}
	"hasSubList: fails on inexact order" {
		assertResult {
			assertThat(iterOf(1, 2, 3)).hasSubList(iterOf(2, 1, 3))
		}.isFailure().hasMessage("""
			|expected to contain the exact sublist (in the same order) as:<[2, 1, 3]>, but found none matching in:<[1, 2, 3]>
		""".trimMargin())

		assertResult {
			assertThat(iterOf(1, 2, 3)).hasSubList(iterOf(3, 2, 1))
		}.isFailure().hasMessage("""
			|expected to contain the exact sublist (in the same order) as:<[3, 2, 1]>, but found none matching in:<[1, 2, 3]>
		""".trimMargin())
	}
	"hasSubList: fails when sublist not found" {
		assertResult {
			assertThat(iterOf(1, 2, 3)).hasSubList(iterOf(3, 4, 5))
		}.isFailure().hasMessage("""
			|expected to contain the exact sublist (in the same order) as:<[3, 4, 5]>, but found none matching in:<[1, 2, 3]>
		""".trimMargin())

		assertResult {
			assertThat(iterOf(1, 2, 3)).hasSubList(iterOf(4, 5, 6))
		}.isFailure().hasMessage("""
			|expected to contain the exact sublist (in the same order) as:<[4, 5, 6]>, but found none matching in:<[1, 2, 3]>
		""".trimMargin())
	}
	//#endregion
})

private fun <T> iterOf(): Iterable<T> = emptyList()
private fun <T> iterOf(element: T): Iterable<T> = listOf(element)
private fun <T> iterOf(vararg elements: T): Iterable<T> = elements.asList()

private fun <T> Iterable<T>.once(): Iterable<T> = if (this is Set) once() else object : IterableOnce<T>() {
	override fun newIterator() = this@once.iterator()
}

private fun <T> Set<T>.once(): Iterable<T> = object : IterableOnce<T>(), Set<T> by this@once {
	override fun iterator() = super.iterator()
	override fun newIterator() = this@once.iterator()
}

private abstract class IterableOnce<out T> : Iterable<T> {
	private var iteratorCalled = false

	override fun iterator(): Iterator<T> = if (!iteratorCalled) {
		iteratorCalled = true
		newIterator()
	} else {
		error("Can only be iterated once")
	}

	protected abstract fun newIterator(): Iterator<T>
}

private class CustomList<E>(private vararg val elements: E) : AbstractList<E>() {
	override val size: Int get() = elements.size

	override fun get(index: Int): E = elements[index]

	override fun equals(other: Any?): Boolean {
		return other is CustomList<*> && super.equals(other)
	}

	override fun hashCode(): Int = elements.hashCode()
}
