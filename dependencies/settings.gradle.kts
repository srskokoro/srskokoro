pluginManagement {
	includeBuild("../gradle/build.support")
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	exportOnly()

	// -=*=-
	// Build foundations

	/** Should correspond to [JavaToolchainSpec] */
	run<Unit> {
		prop("jvm.lang", 21)
		prop("jvm.vendor", JvmVendorSpec.ADOPTIUM)
	}

	val kotlin = "1.9.21" // https://kotlinlang.org/docs/releases.html

	plugin(kotlin("android"), kotlin)
	plugin(kotlin("jvm"), kotlin)
	plugin(kotlin("multiplatform"), kotlin)

	// See also, https://youtrack.jetbrains.com/issue/KT-54691#focus=Comments-27-6852272.0-0
	module(kotlin("gradle-plugins-bom"), kotlin)
	module(kotlin("gradle-plugin"), kotlin)

	plugin(kotlin("plugin.assignment"), kotlin)
	module(kotlin("assignment"), kotlin)
	plugin(kotlin("plugin.sam.with.receiver"), kotlin)
	module(kotlin("sam-with-receiver"), kotlin)

	// See, https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	module(kotlin("bom"), kotlin)
	module(kotlin("stdlib"), kotlin)
	module(kotlin("reflect"), kotlin)
	module(kotlin("test"), kotlin)
	module(kotlin("test-junit5"), kotlin)

	// -=*=-
	// Test infrastructure

	val kotest = "5.8.0" // https://github.com/kotest/kotest/releases
	plugin("io.kotest.multiplatform", kotest)
	module("io.kotest:kotest-runner-junit5", kotest)
	module("io.kotest:kotest-framework-engine", kotest)
	module("io.kotest:kotest-framework-multiplatform-plugin-gradle", kotest)

	val assertk = "0.28.0" // https://github.com/willowtreeapps/assertk/releases
	module("com.willowtreeapps.assertk:assertk", assertk)
}
