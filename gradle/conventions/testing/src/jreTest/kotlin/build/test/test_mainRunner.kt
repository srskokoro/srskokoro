package build.test

import assertk.assertAll
import assertk.assertions.containsExactly
import io.kotest.core.spec.style.FreeSpec

class test_mainRunner : FreeSpec({
	"Prints 'SUCCESS!'" {
		assertAll {
			assertWith {
				mainRunner(::main).start().forwardLines()
			}.containsExactly(SUCCESS)

			assertWith {
				mainRunner<SimpleMain>().start().forwardLines()
			}.containsExactly(SUCCESS)

			assertWith {
				mainRunner<CompanionMain>().start().forwardLines()
			}.containsExactly(SUCCESS)
		}
	}
	"Echoes 'FOO' and 'BAR'" {
		assertAll {
			val args = listOf("FOO", "BAR")
			assertWith {
				mainRunner<EchoMain>(args).start().forwardLines()
			}.hasExactly(args)
		}
	}
	"Fails on invalid `main` functions" {
		assertAll {
			assertResult { mainRunner(::notMain) }.isFailure<IllegalArgumentException>()
			assertResult { mainRunner(SimpleMain::main) }.isFailure<IllegalArgumentException>()
		}
	}
})

// --

private const val SUCCESS = "SUCCESS!"

fun notMain() = Unit

fun main() {
	println(SUCCESS)
}

object SimpleMain {
	@JvmStatic fun main(args: Array<out String>) {
		main()
	}
}

class CompanionMain {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			main()
		}
	}
}

object EchoMain {
	@JvmStatic fun main(vararg args: String) {
		for (x in args) println(x)
	}
}
