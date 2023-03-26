import conv.deps.internal.*

@Suppress("ClassName")
abstract class deps_bundles internal constructor() : deps_bundles_base() {

	val testExtras = bundle {
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-property")
	}
}
