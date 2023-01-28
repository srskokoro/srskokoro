@Suppress("ClassName")
abstract class deps_bundles internal constructor() : deps_bundles_base() {

	val testCommon = bundle {
		module("org.jetbrains.kotlin:kotlin-test")
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-assertions-core")
		module("io.kotest:kotest-property")
	}
}
