package kokoro.internal

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kokoro.internal.io.UnsafeCharArrayWriter
import kokoro.internal.test.support.ThrowableArb
import java.io.PrintWriter
import java.util.LinkedList

class Throwable_spec : FunSpec({
	test("Output of `printSafeStackTrace_fallback` is consistent with `printStackTrace`") {
		checkAll(ThrowableArb()) { throwable ->
			val failures = LinkedList<Throwable>()

			val actual = UnsafeCharArrayWriter().also {
				throwable.printSafeStackTrace_fallback(it, failures::addLast)
			}.toString()

			val expected = UnsafeCharArrayWriter().also {
				throwable.printStackTrace(PrintWriter(it))
			}.toString()

			assertSoftly {
				failures.shouldBeEmpty()
				actual.shouldBe(expected)
			}
		}
	}
})
