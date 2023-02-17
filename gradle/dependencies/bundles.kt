import convention.deps.internal.*

@Suppress("ClassName")
abstract class deps_bundles internal constructor() : deps_bundles_base() {

	val testCommon = bundle {
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-property")
	}
}
