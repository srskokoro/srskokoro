@file:Suppress()

val deps_bundles.testCommon
	get() = init {
		module("org.jetbrains.kotlin:kotlin-test")
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-assertions-core")
		module("io.kotest:kotest-property")
	}
